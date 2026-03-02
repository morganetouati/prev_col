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

        // Prend l'objet le plus grand (le plus proche)
        val best = detectedObjects.maxByOrNull {
            it.boundingBox.height() * it.boundingBox.width()
        } ?: return null

        val bboxHeight = best.boundingBox.height().toFloat()
        val bboxHeightRatio = bboxHeight / imageHeight
        val centerX = best.boundingBox.centerX().toFloat() / imageWidth

        // ML Kit labels génériques : on déduit le type selon taille bbox
        val category = best.labels.firstOrNull()?.text?.lowercase() ?: "person"
        val confidence = best.labels.firstOrNull()?.confidence ?: 0.5f

        // Ignore objets trop petits (< 5% de l'image = trop loin, > 120m)
        if (bboxHeightRatio < 0.05f) return null

        val objectType = mapCategoryToObjectType(category, bboxHeightRatio)

        return CameraDetectionResult(
            category = category,
            confidence = confidence,
            boundingBoxHeightRatio = bboxHeightRatio,
            centerX = centerX,
            objectType = objectType
        )
    }

    /**
     * Mappe la catégorie ML Kit + taille bbox → ObjectType
     * Estimation taille réelle : H_real = (H_ref * f) / H_bbox_pixels
     */
    private fun mapCategoryToObjectType(category: String, heightRatio: Float): ObjectType {
        return when {
            category.contains("person") || category.contains("human") -> {
                // Distingue adulte/enfant/bébé selon la proportion de l'image
                when {
                    heightRatio > 0.60f -> ObjectType.ADULTE   // Occupe >60% → adulte proche
                    heightRatio > 0.35f -> ObjectType.ENFANT   // 35-60% → enfant ou adulte loin
                    heightRatio > 0.15f -> ObjectType.BEBE     // 15-35% → bébé ou enfant loin
                    else -> ObjectType.ADULTE
                }
            }
            category.contains("dog") || category.contains("animal") || category.contains("cat") -> {
                when {
                    heightRatio > 0.40f -> ObjectType.GRAND_CHIEN
                    heightRatio > 0.20f -> ObjectType.MOYEN_CHIEN
                    else -> ObjectType.PETIT_CHIEN
                }
            }
            else -> ObjectType.ADULTE  // Par défaut
        }
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
