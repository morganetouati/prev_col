package com.example.prevcol

import android.os.SystemClock
import kotlin.random.Random

enum class ObjectType(val label: String, val heightRange: Pair<Float, Float>, val icon: String) {
    ADULTE("Adulte", 1.6f to 1.9f, "🧑"),
    ENFANT("Enfant", 1.1f to 1.5f, "🧒"),      // 6-12 ans
    BEBE("Bébé", 0.8f to 1.1f, "👶"),          // 2-5 ans
    PETIT_CHIEN("Petit chien", 0.2f to 0.4f, "🐩"),  // Chihuahua, Yorkshire, etc.
    MOYEN_CHIEN("Chien moyen", 0.4f to 0.6f, "🐕"),  // Beagle, Cocker, etc.
    GRAND_CHIEN("Grand chien", 0.6f to 0.9f, "🐶")   // Labrador, Berger allemand, etc.
}

/**
 * Simule la détection de piétons avec distance et détection d'approches rapides
 * Détecte aussi le type d'objet (adulte/enfant/animal) basé sur la taille
 * REALISTE: Il n'y a pas toujours des objets détectés
 */
object DemoDetectionSimulator {
    private var lastSimulationTime = 0L
    private var currentDistance = 3f
    private var direction = -0.05f
    private var lastDistance = 3f
    private var lastObjectChangeTime = 0L
    private var lastDetectionStateChange = 0L
    
    // État de détection : y a-t-il quelque chose ?
    var hasDetection = false
        private set
    
    // Détection d'approche rapide (silhouette qui grandit vite)
    var isRapidApproach = false
        private set
    
    // Type d'objet détecté et sa taille estimée
    var currentObjectType = ObjectType.ADULTE
        private set
    var objectHeight = 1.75f  // Taille en mètres
        private set
    
    // Position angulaire de l'objet (-90° = extrême gauche, 0° = centre, +90° = extrême droite)
    var currentAngle = 0f
        private set
    private var lastAngle = 0f
    private var angleDirection = 5f  // Vitesse de déplacement horizontal
    
    fun simulateDetection(): Float? {
        val now = SystemClock.elapsedRealtime()
        
        // Change l'état de détection toutes les 20-45 secondes (aléatoire)
        // Intervalles longs pour éviter les fausses détections trop fréquentes
        val detectionInterval = if (hasDetection) Random.nextLong(8000, 20000) else Random.nextLong(20000, 45000)
        if (now - lastDetectionStateChange > detectionInterval || lastDetectionStateChange == 0L) {
            // 5% de chance d'avoir une détection (très réaliste : rarement du monde)
            hasDetection = Random.nextFloat() < 0.05f
            lastDetectionStateChange = now
            
            if (hasDetection) {
                // Nouvel objet détecté : réinitialise la distance à une valeur lointaine
                currentDistance = Random.nextFloat() * 3f + 4f  // 4-7m
                direction = -0.05f  // Approche
                
                // Type aléatoire
                currentObjectType = ObjectType.values().random()
                objectHeight = Random.nextFloat() * 
                    (currentObjectType.heightRange.second - currentObjectType.heightRange.first) + 
                    currentObjectType.heightRange.first
                
                // Position angulaire aléatoire
                currentAngle = Random.nextFloat() * 180f - 90f  // -90° à +90°
                angleDirection = if (Random.nextBoolean()) 5f else -5f
                
                lastObjectChangeTime = now
            }
        }
        
        // Si pas de détection, retourne null
        if (!hasDetection) {
            return null
        }
        
        // Sinon, continue la simulation de l'objet détecté
        if (now - lastSimulationTime > 100) {
            lastDistance = currentDistance
            lastAngle = currentAngle
            
            // Simulation progressive de rapprochement
            currentDistance += direction
            
            // Si l'objet s'éloigne trop (> 10m), il "disparaît"
            if (currentDistance > 10f) {
                hasDetection = false
                lastDetectionStateChange = now
                return null
            }
            
            // Rebond aux extrémités
            if (currentDistance <= 0.5f) {
                direction = 0.05f
            } else if (currentDistance >= 5f) {
                direction = -0.05f
            }
            
            // Déplacement horizontal (gauche-droite)
            currentAngle += angleDirection
            if (currentAngle < -90f || currentAngle > 90f) {
                angleDirection = -angleDirection  // Rebond horizontal
                currentAngle = currentAngle.coerceIn(-90f, 90f)
            }
            
            // Ajout de bruit
            currentDistance += Random.nextFloat() * 0.2f - 0.1f
            currentDistance = currentDistance.coerceIn(0.5f, 10f)
            
            // Détecte approche rapide si distance diminue très vite (> 0.15 par 100ms)
            val distanceChange = lastDistance - currentDistance
            isRapidApproach = distanceChange > 0.15f && currentDistance < 2.0f
            
            lastSimulationTime = now
        }
        
        return currentDistance
    }
    
    /**
     * Retourne les seuils de distance adaptés selon le type d'objet
     * @return Pair<alerte, danger> en mètres
     */
    fun getAdaptedThresholds(): Pair<Float, Float> {
        return when (currentObjectType) {
            ObjectType.ADULTE -> 2.0f to 1.5f         // Alerte à 2m, danger à 1.5m
            ObjectType.ENFANT -> 1.5f to 1.0f         // Alerte à 1.5m, danger à 1m
            ObjectType.BEBE -> 1.2f to 0.8f           // Alerte à 1.2m, danger à 0.8m (petit)
            ObjectType.PETIT_CHIEN -> 0.8f to 0.4f    // Alerte à 0.8m, danger à 0.4m (très petit)
            ObjectType.MOYEN_CHIEN -> 1.2f to 0.7f    // Alerte à 1.2m, danger à 0.7m
            ObjectType.GRAND_CHIEN -> 1.5f to 1.0f    // Alerte à 1.5m, danger à 1m (comme enfant)
        }
    }
    
    enum class MovementDirection {
        APPROACHING,      // S'approche (⬆️)
        RECEDING,        // S'éloigne (⬇️)
        MOVING_LEFT,     // Va vers la gauche (⬅️)
        MOVING_RIGHT,    // Va vers la droite (➡️)
        STATIONARY       // Immobile (-)
    }
    
    /**
     * Retourne la direction principale du mouvement de l'objet
     */
    fun getMovementDirection(): MovementDirection {
        val distanceChange = lastDistance - currentDistance
        val angleChange = currentAngle - lastAngle
        
        // Seuils pour détecter un mouvement significatif
        val distanceThreshold = 0.1f
        val angleThreshold = 2f
        
        // Priorité : distance (approche/éloignement) > angle (latéral)
        return when {
            distanceChange > distanceThreshold -> MovementDirection.APPROACHING
            distanceChange < -distanceThreshold -> MovementDirection.RECEDING
            angleChange > angleThreshold -> MovementDirection.MOVING_RIGHT
            angleChange < -angleThreshold -> MovementDirection.MOVING_LEFT
            else -> MovementDirection.STATIONARY
        }
    }
}

