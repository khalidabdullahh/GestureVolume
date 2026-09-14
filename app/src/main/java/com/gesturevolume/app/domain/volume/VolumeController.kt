package com.gesturevolume.app.domain.volume

import com.gesturevolume.app.data.model.VolumeState
import kotlinx.coroutines.flow.StateFlow

interface VolumeController {
    val volumeState: StateFlow<VolumeState>
    fun refreshCurrentVolume(): VolumeState
    fun adjustVolume(deltaLevels: Int): VolumeState
    fun setVolume(targetLevel: Int): VolumeState
    fun setVolumePercentage(percentage: Int): VolumeState
}
