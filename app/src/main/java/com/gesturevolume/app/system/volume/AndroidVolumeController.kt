package com.gesturevolume.app.system.volume

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.gesturevolume.app.data.model.VolumeState
import com.gesturevolume.app.domain.volume.VolumeController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidVolumeController(private val context: Context) : VolumeController {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val streamType = AudioManager.STREAM_MUSIC

    private val _volumeState = MutableStateFlow(computeCurrentState())
    override val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private fun getMin(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                audioManager.getStreamMinVolume(streamType)
            } catch (_: Exception) {
                0
            }
        } else {
            0
        }
    }

    private fun getMax(): Int {
        return try {
            audioManager.getStreamMaxVolume(streamType)
        } catch (_: Exception) {
            15
        }
    }

    private fun getCurrent(): Int {
        return try {
            audioManager.getStreamVolume(streamType)
        } catch (_: Exception) {
            0
        }
    }

    private fun computeCurrentState(): VolumeState {
        val min = getMin()
        val max = getMax().coerceAtLeast(min + 1)
        val current = getCurrent().coerceIn(min, max)
        val percentage = if (max > min) {
            (((current - min).toFloat() / (max - min).toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

        return VolumeState(
            currentVolume = current,
            minVolume = min,
            maxVolume = max,
            percentage = percentage,
            isMuted = current <= min
        )
    }

    override fun refreshCurrentVolume(): VolumeState {
        val state = computeCurrentState()
        _volumeState.value = state
        return state
    }

    override fun adjustVolume(deltaLevels: Int): VolumeState {
        val current = getCurrent()
        val min = getMin()
        val max = getMax().coerceAtLeast(min + 1)
        val target = (current + deltaLevels).coerceIn(min, max)

        if (target != current) {
            try {
                // Flag 0 prevents default stock system volume UI from flickering
                audioManager.setStreamVolume(streamType, target, 0)
            } catch (_: Exception) {}
        }

        val state = computeCurrentState()
        _volumeState.value = state
        return state
    }

    override fun setVolume(targetLevel: Int): VolumeState {
        val min = getMin()
        val max = getMax().coerceAtLeast(min + 1)
        val target = targetLevel.coerceIn(min, max)

        try {
            audioManager.setStreamVolume(streamType, target, 0)
        } catch (_: Exception) {}

        val state = computeCurrentState()
        _volumeState.value = state
        return state
    }

    override fun setVolumePercentage(percentage: Int): VolumeState {
        val min = getMin()
        val max = getMax().coerceAtLeast(min + 1)
        val target = (min + ((max - min) * (percentage.coerceIn(0, 100) / 100f))).toInt()
        return setVolume(target)
    }
}
