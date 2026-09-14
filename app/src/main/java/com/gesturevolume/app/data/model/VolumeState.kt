package com.gesturevolume.app.data.model

data class VolumeState(
    val currentVolume: Int = 0,
    val minVolume: Int = 0,
    val maxVolume: Int = 15,
    val percentage: Int = 0,
    val isMuted: Boolean = false
) {
    val progress: Float
        get() = if (maxVolume > minVolume) {
            ((currentVolume - minVolume).toFloat() / (maxVolume - minVolume).toFloat()).coerceIn(0f, 1f)
        } else 0f
}
