package com.eddyvn.laixehieuqua.simulation

import com.eddyvn.laixehieuqua.camera.CalibrationSampleCollector
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.engine.DrivePipeline
import com.eddyvn.laixehieuqua.engine.SpeedCalibrationEngine
import org.junit.Assert.*
import org.junit.Test

class SimulationScenariosTest {
    @Test
    fun longRideRepresentsTwoHours(){
        val frames=SimulationScenarios.frames(SimulationScenario.LONG_RIDE)
        assertEquals(7_201,frames.size)
        assertEquals(7_200_000L,frames.last().relativeTimeMs)
    }

    @Test
    fun longRideRunsAllFramesThroughSharedPipeline(){
        val pipeline=DrivePipeline()
        pipeline.reset()
        var lastDistanceKm=0.0

        SimulationScenarios.frames(SimulationScenario.LONG_RIDE)
            .forEach{frame->
                val snapshot=pipeline.consume(
                    sample=frame.toInput(),
                    reference=null,
                    activeCalibration=null,
                )
                assertTrue(snapshot.trueSpeedKmh.isFinite())
                assertTrue(snapshot.distanceKm.isFinite())
                assertTrue(snapshot.trueSpeedKmh in 0.0..180.0)
                assertTrue(snapshot.speedHistoryKmh.size<=40)
                assertTrue(snapshot.ecoTargetHistoryKmh.size<=40)
                lastDistanceKm=snapshot.distanceKm
            }

        assertTrue(lastDistanceKm>50.0)
        assertTrue(lastDistanceKm<130.0)
    }

    @Test
    fun gpsStressContainsDropoutsAndRecoveryDistance(){
        val frames=SimulationScenarios.frames(SimulationScenario.GPS_STRESS)
        assertTrue(frames.any{!it.gpsAvailable})
        assertTrue(frames.any{it.gpsAccuracyM>=45f})
        assertTrue(frames.filter{it.gpsAvailable}.maxOf{it.deltaDistanceM}>20.0)
    }

    @Test
    fun calibrationScenarioProvidesStableOcrPlateaus(){
        val frames=SimulationScenarios.frames(SimulationScenario.CALIBRATION)
        val values=frames.mapNotNull{it.simulatedOcrSpeedKmh}.distinct()
        assertEquals(listOf(32,53,74),values)
    }

    @Test
    fun calibrationScenarioBuildsUsableCalibrationPoints(){
        val pipeline=DrivePipeline()
        val collector=CalibrationSampleCollector()
        val calibrationEngine=SpeedCalibrationEngine()
        val accepted=mutableListOf<com.eddyvn.laixehieuqua.domain.CalibrationPoint>()
        pipeline.reset()

        SimulationScenarios.frames(SimulationScenario.CALIBRATION)
            .forEach{frame->
                val snapshot=pipeline.consume(
                    sample=frame.toInput(),
                    reference=null,
                    activeCalibration=null,
                )
                frame.simulatedOcrSpeedKmh?.let{ocr->
                    collector.offer(
                        timeMs=1_000_000L+frame.relativeTimeMs,
                        trueSpeedKmh=snapshot.trueSpeedKmh,
                        vehicleSpeedKmh=ocr.toDouble(),
                    )?.let(accepted::add)
                }
            }

        assertTrue(accepted.size>=3)
        assertTrue(calibrationEngine.isMonotonic(accepted))
        assertEquals(53.0,calibrationEngine.map(50.0,accepted),2.5)
    }

    @Test
    fun cityTrafficTriggersSharedTrafficDetector(){
        val pipeline=DrivePipeline()
        pipeline.reset()
        var sawTraffic=false

        SimulationScenarios.frames(SimulationScenario.CITY_TRAFFIC)
            .filter{it.gpsAvailable}
            .forEach{frame->
                val snapshot=pipeline.consume(
                    sample=frame.toInput(),
                    reference=null,
                    activeCalibration=null,
                )
                sawTraffic=sawTraffic||snapshot.traffic
            }

        assertTrue(sawTraffic)
    }

    @Test
    fun clearRoadStaysOutOfTrafficAndAccumulatesDistance(){
        val pipeline=DrivePipeline()
        pipeline.reset()
        var lastDistance=0.0

        SimulationScenarios.frames(SimulationScenario.CLEAR_ROAD)
            .forEach{frame->
                val snapshot=pipeline.consume(
                    sample=frame.toInput(),
                    reference=null,
                    activeCalibration=null,
                )
                assertFalse(snapshot.traffic)
                lastDistance=snapshot.distanceKm
            }

        assertTrue(lastDistance>2.0)
    }

    private fun SimulationFrame.toInput()=DriveInputSample(
        timestampMs=1_000_000L+relativeTimeMs,
        rawGpsSpeedKmh=rawGpsSpeedKmh,
        gpsAccuracyM=gpsAccuracyM,
        accelerationMs2=accelerationMs2,
        leanDeg=leanDeg,
        deltaDistanceM=deltaDistanceM,
    )
}
