package com.example.prevcol

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Détecteur de personnes/animaux via CameraX + ML Kit
 * Traitement 100% on-device, aucune donnée ne quitte le téléphone
 */
class CameraDetector(
    private val context: Context,
    private val onDetection: (CameraDetectionResult?) -> Unit
) {
    data class CameraDetectionResult(
        val category: String,       // "person", "animal", etc.
        val confidence: Float,      // 0.0 - 1.0
        val boundingBoxHeightRatio: Float,  // Hauteur bbox / hauteur image (pour distance)
        val centerX: Float,         // Position X normalisée (0=gauche, 1=droite)
        val objectType: ObjectType  // Type mappé
    )

    private var objectDetector: ObjectDetector? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var isRunning = false

    // Intervalle de traitement (toutes les 300ms pour économiser la batterie)
    private var lastProcessTime = 0L
    private val processIntervalMs = 300L

    fun start(lifecycleOwner: LifecycleOwner) {
        if (isRunning) return
        isRunning = true
        lastProcessTime = 0L

        if (cameraExecutor.isShutdown || cameraExecutor.isTerminated) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        objectDetector?.close()

        // Configure ML Kit Object Detector (mode stream pour temps réel)
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()  // ACTIVER la classification pour distinguer personne/animal
            .build()

        objectDetector = ObjectDetection.getClient(options)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCamera(lifecycleOwner)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProvider = cameraProvider ?: return

        // Caméra arrière, résolution basse (économie batterie)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            val now = System.currentTimeMillis()
            if (now - lastProcessTime > processIntervalMs) {
                lastProcessTime = now
                processImage(imageProxy)
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalyzer)
        } catch (e: Exception) {
            Log.e("CameraDetector", "Échec liaison caméra: ${e.message}")
            // Fallback sur simulateur si la caméra échoue
            onDetection(null)
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val imageHeight = imageProxy.height.toFloat()
        val imageWidth = imageProxy.width.toFloat()

        objectDetector?.process(image)
            ?.addOnSuccessListener { detectedObjects ->
                val result = selectBestDetection(detectedObjects, imageHeight, imageWidth)
                onDetection(result)
            }
            ?.addOnFailureListener {
                onDetection(null)
            }
            ?.addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun selectBestDetection(
        detectedObjects: List<DetectedObject>,
        imageHeight: Float,
        imageWidth: Float
    ): CameraDetectionResult? {
        if (detectedObjects.isEmpty()) return null

        // Filtre les objets selon des critères stricts pour ne garder que les êtres vivants
        val candidates = detectedObjects.mapNotNull { obj ->
            val bboxH = obj.boundingBox.height().toFloat()
            val bboxW = obj.boundingBox.width().toFloat()
            val hRatio = bboxH / imageHeight
            val wRatio = bboxW / imageWidth
            val aspect = bboxW / bboxH  // largeur/hauteur
            val topRatio = obj.boundingBox.top.toFloat() / imageHeight
            val bottomRatio = obj.boundingBox.bottom.toFloat() / imageHeight

            // REJETER les faux positifs :
            
            // 1. Trop petit (< 8%) = bruit ou objet loin
            if (hRatio < 0.08f) return@mapNotNull null
            
            // 2. Trop grand (> 85% de l'image en hauteur ET largeur) = fond/mur/sol
            if (hRatio > 0.85f && wRatio > 0.85f) return@mapNotNull null
            
            // 3. Objet qui remplit presque toute la largeur = background (mur, sol, plafond)
            if (wRatio > 0.90f) return@mapNotNull null
            
            // 4. Aspect ratio trop extrême : trop large (> 3:1) = barre/meuble
            if (aspect > 3.0f) return@mapNotNull null
            
            // 5. Bbox qui commence tout en haut ET finit tout en bas = tout l'écran = background
            if (topRatio < 0.05f && bottomRatio > 0.95f && wRatio > 0.6f) return@mapNotNull null

            // ML Kit labels
            val label = obj.labels.firstOrNull()
            val category = label?.text?.lowercase() ?: "unknown"
            val confidence = label?.confidence ?: 0.0f

            // 6. Ignorer les catégories ML Kit qui ne sont clairement PAS des êtres vivants
            // ML Kit base : "Fashion good"(0), "Food"(1), "Home good"(2), "Place"(3), "Plant"(4)
            if (category == "place" || category == "food" || category == "plant" || category == "home good") return@mapNotNull null

            // 7. Rejeter les labels à basse confiance (détection floue/incertaine)
            if (label != null && confidence < 0.40f) return@mapNotNull null

            // 8. Pour les objets sans label (inconnus), n'accepter que si la géométrie
            //    est fortement compatible avec une silhouette humaine debout :
            //    bounding box verticale (aspect < 0.65), dans la moitié haute à centrale de l'image
            if (label == null) {
                if (!(aspect < 0.65f && topRatio < 0.55f && hRatio > 0.10f)) return@mapNotNull null
            }

            val centerX = obj.boundingBox.centerX().toFloat() / imageWidth
            
            val objectType = mapToObjectType(category, label?.index ?: -1, hRatio, aspect, topRatio)

            CameraDetectionResult(
                category = category,
                confidence = confidence,
                boundingBoxHeightRatio = hRatio,
                centerX = centerX,
                objectType = objectType
            )
        }

        // Prend le candidat le plus grand (le plus proche), s'il y en a
        return candidates.maxByOrNull { it.boundingBoxHeightRatio }
    }

    /**
     * Détecte le type d'objet en combinant 3 indices :
     * 1. Aspect ratio de la bbox (largeur/hauteur) : humain vertical, animal horizontal
     * 2. Taille relative dans l'image (hauteur bbox / hauteur image)
     * 3. Position verticale : un animal/chien est dans la moitié BASSE de l'image,
     *    un humain debout occupe de haut en bas
     */
    private fun mapToObjectType(
        category: String,
        categoryIndex: Int,
        heightRatio: Float,
        aspectRatio: Float,
        topRatio: Float
    ): ObjectType {
        // Critères simples et fiables :
        // - Humain debout = bbox VERTICAL (aspect < 0.65), commence en haut de l'image
        // - Animal = bbox HORIZONTAL (aspect > 0.85), positionné dans la moitié basse
        
        // "Fashion good" (vêtements) → très forte indication d'un humain
        if (category == "fashion good" && aspectRatio < 0.80f) {
            return when {
                heightRatio > 0.45f -> ObjectType.ADULTE
                heightRatio > 0.20f -> ObjectType.ENFANT
                else -> ObjectType.ADULTE
            }
        }

        // Animal : horizontal + bas dans l'image
        if (aspectRatio > 0.85f && topRatio > 0.35f) {
            return when {
                heightRatio > 0.30f -> ObjectType.GRAND_CHIEN
                heightRatio > 0.15f -> ObjectType.MOYEN_CHIEN
                else -> ObjectType.PETIT_CHIEN
            }
        }

        // Humain debout : bounding box verticale (plus haute que large)
        // topRatio relaxé jusqu'à 0.55 pour capter l'approche frontale directe :
        // quand une personne marche VERS vous, sa tête peut être au centre de l'image
        if (aspectRatio < 0.70f && topRatio < 0.55f) {
            return when {
                heightRatio > 0.45f -> ObjectType.ADULTE
                heightRatio > 0.20f -> ObjectType.ENFANT
                else -> ObjectType.ADULTE  // silhouette verticale petite = adulte lointain
            }
        }

        // Cas ambigu : ne pas classer comme ADULTE pour éviter les faux positifs
        // Un objet qui ne ressemble clairement ni à une personne ni à un animal est ignoré
        // (MOYEN_CHIEN ne déclenchera pas d'alerte puisqu'on ne vibre que pour les personnes)
        return ObjectType.MOYEN_CHIEN
    }

    /**
     * Estime la distance via taille bbox + correction perspective
     * Formule calibrée : d = (H_ref * focal_norm) / bbox_height_ratio
     * Avec correction pour les bords de l'image (objets sur les côtés apparaissent plus petits)
     */
    fun estimateDistance(result: CameraDetectionResult): Float {
        // Hauteur de référence par type (mètres)
        val refHeight = when (result.objectType) {
            ObjectType.ADULTE -> 1.75f
            ObjectType.ENFANT -> 1.30f
            ObjectType.BEBE -> 0.90f
            ObjectType.PETIT_CHIEN -> 0.30f
            ObjectType.MOYEN_CHIEN -> 0.50f
            ObjectType.GRAND_CHIEN -> 0.70f
        }

        // Facteur focal normalisé (calibré pour angle de vue ~60° smartphone standard)
        val fNorm = 0.35f

        // Correction perspective : objet sur le côté paraît plus petit (correction cosinus)
        val angleFromCenter = Math.abs(result.centerX - 0.5f) * 2f  // 0=centre, 1=bord
        val perspectiveCorrection = 1f + (angleFromCenter * 0.15f)  // +0-15% sur les bords

        val rawDistance = (refHeight * fNorm) / result.boundingBoxHeightRatio
        val correctedDistance = rawDistance * perspectiveCorrection

        return correctedDistance.coerceIn(0.3f, 15f)
    }

    /**
     * Convertit la position X normalisée en angle (-90° gauche, 0° centre, +90° droite)
     */
    fun estimateAngle(centerX: Float): Float {
        return (centerX - 0.5f) * 160f  // -80° à +80° (garde une marge)
    }

    fun stop() {
        isRunning = false
        cameraProvider?.unbindAll()
        objectDetector?.close()
        objectDetector = null
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdownNow()
        }
    }
}
