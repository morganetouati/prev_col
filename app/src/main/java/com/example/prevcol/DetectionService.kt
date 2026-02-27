package com.example.prevcol

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.math.sqrt

class DetectionService : Service(), SensorEventListener {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private lateinit var radarOverlay: RadarOverlay
    private lateinit var gameStats: GameStats
    
    // Lifecycle pour CameraX (requis par ProcessCameraProvider.bindToLifecycle)
    private lateinit var lifecycleRegistry: LifecycleRegistry
    private lateinit var serviceLifecycleOwner: LifecycleOwner

    // Détecteur caméra ML Kit (null si permission caméra absente)
    private var cameraDetector: CameraDetector? = null
    private var useCameraDetection = false
    private var lastCameraResult: CameraDetector.CameraDetectionResult? = null
    private var lastCameraResultTime = 0L
    private val cameraResultTimeoutMs = 2000L  // Si pas de résultat caméra depuis 2s → no detection
    
    // Détecteur de mouvement
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isWalking = false
    private var lastMotionTime = 0L
    private val motionThreshold = 1.5f  // Seuil de mouvement (m/s²)
    private val immobileTimeoutMs = 3000L  // 3 secondes sans mouvement = immobile
    
    // Détection de poche (capteur proximité)
    private var proximitySensor: Sensor? = null
    private var isInPocket = false
    
    private var lastVibrationTime = 0L
    private val minVibrationIntervalMs = 3000L
    private var isDanger = false
    private var lastRapidApproachTime = 0L
    private val minRapidApproachIntervalMs = 2000L  // Max une approche rapide tous les 2s
    
    // Deux types de notifications
    private val SERVICE_NOTIFICATION_ID = 1
    private val ALERT_NOTIFICATION_ID = 2
    private val SERVICE_CHANNEL_ID = "service_channel"
    private val ALERT_CHANNEL_ID = "alert_channel"
    
    private var isAlertShowing = false

    override fun onCreate() {
        super.onCreate()
        // Initialise le lifecycle pour CameraX
        serviceLifecycleOwner = object : LifecycleOwner {
            override fun getLifecycle(): Lifecycle = lifecycleRegistry
        }
        lifecycleRegistry = LifecycleRegistry(serviceLifecycleOwner)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        radarOverlay = RadarOverlay(this)
        gameStats = GameStats(this)
        createNotificationChannel()
        
        // Initialise le détecteur de mouvement et proximité
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        // Tente d'activer la détection caméra si permission accordée
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            useCameraDetection = true
            cameraDetector = CameraDetector(this) { result ->
                lastCameraResult = result
                if (result != null) lastCameraResultTime = SystemClock.elapsedRealtime()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            startDetection()
        } else if (intent?.action == ACTION_STOP) {
            stopDetection()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(android.app.NotificationManager::class.java)
            
            // Canal pour la notification de service (discrète, silencieuse)
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Service de surveillance",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.description = "Notification discrète pour indiquer que la surveillance est active"
            serviceChannel.setShowBadge(false)
            manager.createNotificationChannel(serviceChannel)
            
            // Canal pour les alertes (bannière HeadsUp avec son/vibration)
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alertes de proximité",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            alertChannel.description = "Alertes quand quelqu'un s'approche trop près"
            alertChannel.enableVibration(false) // Vibration gérée manuellement
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun startDetection() {
        if (isRunning) return
        isRunning = true
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        // Démarre avec la notification de service permanente
        updateServiceNotification()
        
        // Active le détecteur de mouvement et proximité
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        // Démarre la caméra si disponible (sur le main thread)
        if (useCameraDetection) {
            cameraDetector?.start(serviceLifecycleOwner)
        }
        
        handler.post(detectionPoll)
    }

    private fun stopDetection() {
        if (!isRunning) return
        isRunning = false
        handler.removeCallbacks(detectionPoll)
        radarOverlay.hide()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        
        // Arrête la caméra
        cameraDetector?.stop()
        
        // Désactive le détecteur de mouvement
        sensorManager.unregisterListener(this)
        
        // Annule les deux notifications
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private val detectionPoll = object : Runnable {
        override fun run() {
            if (isRunning) {
                // Si téléphone dans la poche, mettre en veille
                if (isInPocket) {
                    radarOverlay.hide()
                    handler.postDelayed(this, 500L)
                    return
                }
                
                val now = SystemClock.elapsedRealtime()
                if (now - lastMotionTime > immobileTimeoutMs) {
                    isWalking = false
                }
                
                // SOURCE DE DÉTECTION : caméra si disponible, simulateur sinon
                val distance: Float?
                val objectType: ObjectType
                val height: Float
                val angle: Float
                val hasDetection: Boolean
                val direction: DemoDetectionSimulator.MovementDirection
                val isRapidApproach: Boolean

                if (useCameraDetection) {
                    val camResult = lastCameraResult
                    val camFresh = (now - lastCameraResultTime) < cameraResultTimeoutMs
                    if (camResult != null && camFresh) {
                        distance = cameraDetector!!.estimateDistance(camResult)
                        objectType = camResult.objectType
                        height = objectType.heightRange.first
                        angle = cameraDetector!!.estimateAngle(camResult.centerX)
                        hasDetection = true
                        direction = DemoDetectionSimulator.getMovementDirection()
                        isRapidApproach = distance < 1.0f && camResult.confidence > 0.7f
                    } else {
                        distance = null
                        objectType = ObjectType.ADULTE
                        height = 1.75f
                        angle = 0f
                        hasDetection = false
                        direction = DemoDetectionSimulator.MovementDirection.STATIONARY
                        isRapidApproach = false
                    }
                } else {
                    // Fallback simulateur
                    val simDist = DemoDetectionSimulator.simulateDetection()
                    distance = simDist
                    objectType = DemoDetectionSimulator.currentObjectType
                    height = DemoDetectionSimulator.objectHeight
                    angle = DemoDetectionSimulator.currentAngle
                    hasDetection = DemoDetectionSimulator.hasDetection
                    direction = DemoDetectionSimulator.getMovementDirection()
                    isRapidApproach = DemoDetectionSimulator.isRapidApproach
                }
                
                updateServiceNotification(useCameraDetection, hasDetection)
                
                if (distance == null || !hasDetection) {
                    radarOverlay.hide()
                    val notificationManager = NotificationManagerCompat.from(this@DetectionService)
                    if (isAlertShowing) {
                        notificationManager.cancel(ALERT_NOTIFICATION_ID)
                        isAlertShowing = false
                    }
                    isDanger = false
                } else {
                    updateAlertNotification(distance, isRapidApproach)
                    if (isWalking) {
                        radarOverlay.show()
                        radarOverlay.updateDetections(distance, angle, objectType, height, direction)
                    } else {
                        radarOverlay.hide()
                    }
                    handleAlerts(distance, isRapidApproach)
                }
                
                handler.postDelayed(this, 50)
            }
        }
    }

    private fun updateServiceNotification(usingCamera: Boolean = false, hasDetection: Boolean = false) {
        // Notification discrète permanente (icône œil dans la status bar)
        val stopIntent = Intent(this, DetectionService::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val statusText = when {
            !isWalking -> "📋 Téléphone posé - En veille"
            hasDetection -> if (usingCamera) "👀 Caméra active - Détection!" else "👀 En marche - Détection active"
            else -> if (usingCamera) "👁️ Caméra active - RAS" else "🚶 En marche - RAS (rien autour)"
        }
        
        val notification = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("👁️ Regards au monde")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_menu_view)  // Icône œil
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .addAction(android.R.drawable.ic_delete, "Arrêter", stopPendingIntent)
            .build()
        
        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }
    
    private fun updateAlertNotification(distanceM: Float, isRapidApproach: Boolean) {
        val notificationManager = NotificationManagerCompat.from(this)
        
        // Si le téléphone n'est pas en marche, annule toutes les alertes
        if (!isWalking) {
            if (isAlertShowing) {
                notificationManager.cancel(ALERT_NOTIFICATION_ID)
                isAlertShowing = false
            }
            return
        }
        
        // Récupère les seuils adaptés au type d'objet détecté
        val thresholds = DemoDetectionSimulator.getAdaptedThresholds()
        val alertThreshold = thresholds.first   // Ex: 2m pour adulte, 1.5m pour enfant
        val dangerThreshold = thresholds.second // Ex: 1.5m pour adulte, 1m pour enfant
        
        val objectType = DemoDetectionSimulator.currentObjectType
        val objectHeight = DemoDetectionSimulator.objectHeight
        
        val newIsDanger = distanceM <= alertThreshold
        isDanger = newIsDanger
        
        if (!isDanger) {
            // Chemin libre : annule la notification d'alerte si elle existe
            if (isAlertShowing) {
                notificationManager.cancel(ALERT_NOTIFICATION_ID)
                isAlertShowing = false
            }
            return
        }
        
        // Quelqu'un approche : affiche/met à jour la bannière d'alerte
        isAlertShowing = true
        
        // Icône selon le type d'objet (depuis l'enum)
        val objectIcon = objectType.icon
        
        val colorResId = when {
            isRapidApproach -> android.R.color.holo_red_light  // 🔴 Rouge vif
            distanceM < dangerThreshold -> android.R.color.holo_red_light  // 🔴 Rouge (danger)
            distanceM < alertThreshold -> android.R.color.holo_orange_light  // 🟠 Orange (alerte)
            else -> android.R.color.holo_green_light
        }
        
        val title = when {
            isRapidApproach -> "$objectIcon ⚡ APPROCHE RAPIDE!"
            distanceM < dangerThreshold -> "$objectIcon 🔴 DANGER: ${objectType.label} très proche!"
            distanceM < alertThreshold -> "$objectIcon 🟠 ALERTE: ${objectType.label} s'approche"
            else -> "$objectIcon Attention"
        }
        val message = String.format("%s (%.2fm) - Distance: %.1f m", objectType.label, objectHeight, distanceM)
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alertNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)  // Icône alerte
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(ContextCompat.getColor(this, colorResId))
            .setFullScreenIntent(pendingIntent, distanceM < dangerThreshold)  // HeadsUp si danger
            .build()
        
        notificationManager.notify(ALERT_NOTIFICATION_ID, alertNotification)
    }

    private fun handleAlerts(distanceM: Float, isRapidApproach: Boolean) {
        if (!isWalking) return
        val now = SystemClock.elapsedRealtime()
        val thresholds = DemoDetectionSimulator.getAdaptedThresholds()
        val dangerThreshold = thresholds.second
        val objectType = DemoDetectionSimulator.currentObjectType

        if (distanceM < dangerThreshold && now - lastVibrationTime > minVibrationIntervalMs) {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            // Pattern de vibration selon le type d'objet
            val pattern: LongArray = when {
                isRapidApproach -> longArrayOf(0, 500)             // Longue continue
                objectType == ObjectType.ADULTE -> longArrayOf(0, 200, 100, 200, 100, 200)  // 3x
                objectType == ObjectType.ENFANT -> longArrayOf(0, 80, 60, 80, 60, 80, 60, 80, 60, 80)  // 5x courts
                objectType == ObjectType.BEBE   -> longArrayOf(0, 60, 40, 60, 40, 60, 40, 60)  // 4x très courts
                objectType == ObjectType.PETIT_CHIEN -> longArrayOf(0, 100, 150, 100)       // 2x courts espacés
                objectType == ObjectType.MOYEN_CHIEN -> longArrayOf(0, 150, 100, 150)       // 2x moyens
                objectType == ObjectType.GRAND_CHIEN -> longArrayOf(0, 200, 100, 200)       // 2x longs
                else -> longArrayOf(0, 200)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitudes = IntArray(pattern.size) { if (it % 2 == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE }
                v.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION") v.vibrate(pattern, -1)
            }
            lastVibrationTime = now
            gameStats.recordAlert("danger")

            // Bip adapté
            val norm = (distanceM / dangerThreshold).coerceIn(0f, 1f)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, (50 + norm * 300).toInt())
        }

        if (isRapidApproach && now - lastRapidApproachTime > minRapidApproachIntervalMs) {
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") v.vibrate(100)
            }
            lastRapidApproachTime = now
            gameStats.recordAlert("rapid")
            tone.startTone(ToneGenerator.TONE_DTMF_1, 100)
        }
    }

    // Détection de mouvement et proximité (poche)
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_PROXIMITY -> {
                // Capteur proximité: détecte si le téléphone est dans une poche
                val maxRange = event.sensor.maximumRange
                isInPocket = event.values[0] < maxRange
                if (isInPocket) {
                    radarOverlay.hide()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                // Calcule la magnitude du mouvement (retire la gravité ~9.8 m/s²)
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                
                // Si mouvement détecté au-dessus du seuil
                if (acceleration > motionThreshold) {
                    isWalking = true
                    lastMotionTime = SystemClock.elapsedRealtime()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Pas nécessaire pour l'accéléromètre
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection()
        tone.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.prevcol.START"
        const val ACTION_STOP = "com.example.prevcol.STOP"
    }
}
