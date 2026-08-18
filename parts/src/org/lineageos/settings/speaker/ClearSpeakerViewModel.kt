/*
 * SPDX-FileCopyrightText: 2025 Paranoid Android
 *                         2026 zylhdrXP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.speaker

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.lineageos.settings.R
import java.io.IOException

class ClearSpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val stopRunnable = Runnable {
        stopPlaying()
    }

    fun toggleClearSpeaker(enabled: Boolean) {
        if (enabled) {
            if (startPlaying()) {
                handler.postDelayed(stopRunnable, 30000)
            }
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying(): Boolean {
        try {
            audioManager.setParameters("status_earpiece_clean=on")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setLooping(true)
                val file = context.resources.openRawResourceFd(R.raw.clear_speaker_sound)
                setDataSource(file.fileDescriptor, file.startOffset, file.length)
                file.close()
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
            _isPlaying.value = true
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to play speaker clean sound!", e)
            stopPlaying()
            return false
        }
    }

    fun stopPlaying() {
        handler.removeCallbacks(stopRunnable)
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        mediaPlayer = null
        audioManager.setParameters("status_earpiece_clean=off")
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaying()
    }

    companion object {
        private const val TAG = "ClearSpeakerViewModel"
    }
}
