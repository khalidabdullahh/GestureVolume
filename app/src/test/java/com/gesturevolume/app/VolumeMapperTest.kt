package com.gesturevolume.app

import com.gesturevolume.app.domain.volume.VolumeMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VolumeMapperTest {

    private lateinit var mapper: VolumeMapper

    @Before
    fun setUp() {
        mapper = VolumeMapper(pixelThresholdPerStep = 60f)
    }

    @Test
    fun testSubThresholdMovement_producesZeroDelta() {
        val delta = mapper.calculateDeltaLevel(deltaY = 25f, sensitivityMultiplier = 1.0f)
        assertEquals(0, delta)
    }

    @Test
    fun testUpwardMovement_producesPositiveSteps() {
        val delta = mapper.calculateDeltaLevel(deltaY = 140f, sensitivityMultiplier = 1.0f)
        assertEquals(2, delta)
    }

    @Test
    fun testDownwardMovement_producesNegativeSteps() {
        val delta = mapper.calculateDeltaLevel(deltaY = -180f, sensitivityMultiplier = 1.0f)
        assertEquals(-3, delta)
    }

    @Test
    fun testHighSensitivity_requiresFewerPixels() {
        // Sensitivity 2.0x -> effective threshold is 30px
        val delta = mapper.calculateDeltaLevel(deltaY = 70f, sensitivityMultiplier = 2.0f)
        assertEquals(2, delta)
    }

    @Test
    fun testPercentageCalculation() {
        val percent = mapper.calculatePercentageDelta(deltaY = 400f, screenHeightPx = 2000f, sensitivityMultiplier = 1.0f)
        assertTrue(percent in 45..55)
    }
}
