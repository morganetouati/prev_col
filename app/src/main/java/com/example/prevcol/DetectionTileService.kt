package com.example.prevcol

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

/**
 * Quick Settings Tile pour activer/désactiver la surveillance
 */
class DetectionTileService : TileService() {
    
    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }
    
    override fun onClick() {
        super.onClick()
        
        val tile = qsTile
        val isActive = tile.state == Tile.STATE_ACTIVE
        
        if (isActive) {
            // Arrêter le service
            val stopIntent = Intent(this, DetectionService::class.java)
            stopIntent.action = DetectionService.ACTION_STOP
            startService(stopIntent)
            
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Regards au monde"
            tile.contentDescription = "Activer la surveillance"
        } else {
            // Démarrer le service
            val startIntent = Intent(this, DetectionService::class.java)
            startIntent.action = DetectionService.ACTION_START
            ContextCompat.startForegroundService(this, startIntent)
            
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Surveillance ON"
            tile.contentDescription = "Désactiver la surveillance"
        }
        
        tile.updateTile()
    }
    
    private fun updateTileState() {
        val tile = qsTile ?: return
        
        // Par défaut, considère inactif (à améliorer avec SharedPreferences si besoin)
        tile.state = Tile.STATE_INACTIVE
        tile.label = "Regards au monde"
        tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_view)
        tile.contentDescription = "Activer la surveillance piéton"
        tile.updateTile()
    }
}
