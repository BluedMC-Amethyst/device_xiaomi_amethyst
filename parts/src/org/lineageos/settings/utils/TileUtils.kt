package org.lineageos.settings.utils

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.widget.Toast
import org.lineageos.settings.R

object TileUtils {
    fun requestAddTileService(context: Context, tileServiceClass: Class<*>, labelResId: Int, iconResId: Int) {
        val manager = context.getSystemService(StatusBarManager::class.java) ?: return
        manager.requestAddTileService(
            ComponentName(context, tileServiceClass), context.getString(labelResId),
            Icon.createWithResource(context, iconResId), context.mainExecutor
        ) { result ->
            val message = when (result) {
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> R.string.tile_added
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> R.string.tile_not_added
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.tile_already_added
                else -> return@requestAddTileService
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
