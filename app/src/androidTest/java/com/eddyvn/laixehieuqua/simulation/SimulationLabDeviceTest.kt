package com.eddyvn.laixehieuqua.simulation

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eddyvn.laixehieuqua.LaiXeApp
import com.eddyvn.laixehieuqua.camera.CalibrationSampleCollector
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.engine.DrivePipeline
import com.eddyvn.laixehieuqua.engine.SpeedCalibrationEngine
import com.eddyvn.laixehieuqua.ui.MainViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimulationLabDeviceTest {
    private val app: LaiXeApp
        get() = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as LaiXeApp

    @Test
    fun allScenariosCompleteWithoutWritingRideOrFuelData() = runBlocking {
        val graph = app.graph
        val countsBefore = realDataCounts()

        SimulationScenario.entries.forEach { scenario ->
            graph.simulationController.startScenario(scenario, speedMultiplier = 100)
            val timeoutMs = if (scenario == SimulationScenario.LONG_RIDE) 120_000L else 15_000L
            val result = withTimeout(timeoutMs) {
                graph.simulationController.state.first {
                    !it.active && it.message == "Completed"
                }
            }
            val snapshot = graph.driveStateStore.state.value

            assertEquals("Scenario: ${scenario.title}", result.source)
            assertEquals(1f, result.progress)
            assertTrue("Non-finite speed in ${scenario.title}", snapshot.trueSpeedKmh.isFinite())
            assertTrue("Speed outside bounds in ${scenario.title}", snapshot.trueSpeedKmh in 0.0..180.0)
            assertTrue("History grew in ${scenario.title}", snapshot.speedHistoryKmh.size <= 40)
            if (scenario == SimulationScenario.LONG_RIDE) {
                assertEquals(7_200_000L, result.elapsedScenarioMs)
                assertEquals(7_200_000L, result.totalScenarioMs)
            }
            if (scenario == SimulationScenario.CALIBRATION) {
                assertEquals(74, result.lastOcrSpeedKmh)
            }
            Log.i(TAG, "${scenario.name}: completed at ${result.elapsedScenarioMs}ms")
        }

        assertEquals(countsBefore, realDataCounts())
    }

    @Test
    fun speedPauseResumeStopAndEmptyReplayAreControllableWithoutUi() = runBlocking {
        val controller = app.graph.simulationController
        listOf(1, 5, 20, 100).forEach { speed ->
            controller.setSpeedMultiplier(speed)
            assertEquals(speed, controller.state.value.speedMultiplier)
        }

        controller.startScenario(SimulationScenario.CITY_TRAFFIC, speedMultiplier = 100)
        assertTrue(controller.state.value.active)
        controller.togglePause()
        assertTrue(controller.state.value.paused)
        controller.togglePause()
        assertFalse(controller.state.value.paused)
        controller.stop()
        assertEquals("Stopped", controller.state.value.message)
        assertFalse(controller.state.value.active)

        controller.replay(emptyList(), speedMultiplier = 100)
        assertEquals("No recorded ride is available yet", controller.state.value.message)
        assertFalse(controller.state.value.active)

        controller.replay(
            listOf(
                SimulationFrame(0L, 10.0, deltaDistanceM = 1.0),
                SimulationFrame(1_000L, 20.0, deltaDistanceM = 2.0),
                SimulationFrame(2_000L, 30.0, deltaDistanceM = 3.0),
            ),
            speedMultiplier = 100,
        )
        val replay = withTimeout(5_000L) {
            controller.state.first { !it.active && it.message == "Completed" }
        }
        assertEquals("Replay: last recorded ride", replay.source)
        assertEquals(2_000L, replay.elapsedScenarioMs)
        assertTrue(app.graph.driveStateStore.state.value.distanceKm > 0.0)

        MainViewModel(app).replayLastRide(100)
        val noRecordedRide = withTimeout(5_000L) {
            controller.state.first { it.message == "No recorded ride is available yet" }
        }
        assertEquals("Replay", noRecordedRide.source)
    }

    @Test
    fun calibrationScenarioFlowsThroughCollectorAndCalibrationEngine() {
        val pipeline = DrivePipeline()
        val collector = CalibrationSampleCollector()
        val calibration = SpeedCalibrationEngine()
        val points = mutableListOf<com.eddyvn.laixehieuqua.domain.CalibrationPoint>()
        pipeline.reset()

        SimulationScenarios.frames(SimulationScenario.CALIBRATION).forEach { frame ->
            val snapshot = pipeline.consume(
                sample = DriveInputSample(
                    timestampMs = 1_000_000L + frame.relativeTimeMs,
                    rawGpsSpeedKmh = frame.rawGpsSpeedKmh,
                    gpsAccuracyM = frame.gpsAccuracyM,
                    accelerationMs2 = frame.accelerationMs2,
                    leanDeg = frame.leanDeg,
                    deltaDistanceM = frame.deltaDistanceM,
                ),
                reference = null,
                activeCalibration = null,
            )
            frame.simulatedOcrSpeedKmh?.let { ocr ->
                collector.offer(
                    timeMs = 1_000_000L + frame.relativeTimeMs,
                    trueSpeedKmh = snapshot.trueSpeedKmh,
                    vehicleSpeedKmh = ocr.toDouble(),
                )?.let(points::add)
            }
        }

        assertTrue(calibration.isMonotonic(points))
        assertTrue(points.size >= 3)
        assertEquals(53.0, calibration.map(50.0, points), 2.5)
    }

    private fun realDataCounts(): List<Int> {
        val db = app.graph.database.openHelper.readableDatabase
        return listOf("sessions", "track_points", "fuel_entries").map { table ->
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }
        }
    }

    private companion object {
        const val TAG = "SimulationLabTest"
    }
}
