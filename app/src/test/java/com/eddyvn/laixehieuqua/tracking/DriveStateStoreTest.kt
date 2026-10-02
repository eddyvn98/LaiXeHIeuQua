package com.eddyvn.laixehieuqua.tracking

import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveStateStoreTest {
    @Test
    fun realtimeMotionTracksAverageAccelerationMagnitude(){
        val store=DriveStateStore()
        store.beginSession(1_000L)
        store.update(
            DriveSnapshot(
                tracking=true,
                displaySpeedKmh=20.0,
                averageSpeedKmh=20.0,
            )
        )

        store.updateRealtimeMotion(20.0,1.0)
        store.updateRealtimeMotion(22.0,-3.0)

        assertEquals(-3.0,store.state.value.accelerationMs2,0.001)
        assertEquals(2.0,store.state.value.averageAccelerationMs2,0.001)
    }

    @Test
    fun gpsSnapshotDoesNotEraseRealtimeAverageAcceleration(){
        val store=DriveStateStore()
        store.beginSession(1_000L)
        store.updateRealtimeMotion(15.0,2.0)

        store.update(
            DriveSnapshot(
                tracking=true,
                displaySpeedKmh=16.0,
                averageSpeedKmh=15.5,
            )
        )

        assertEquals(2.0,store.state.value.averageAccelerationMs2,0.001)
    }

    @Test
    fun newSessionResetsAverageAcceleration(){
        val store=DriveStateStore()
        store.beginSession(1_000L)
        store.updateRealtimeMotion(25.0,2.5)
        assertTrue(store.state.value.averageAccelerationMs2>0.0)

        store.beginSession(2_000L)

        assertEquals(0.0,store.state.value.averageAccelerationMs2,0.001)
    }
}
