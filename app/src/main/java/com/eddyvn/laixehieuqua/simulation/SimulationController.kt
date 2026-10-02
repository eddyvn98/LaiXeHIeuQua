package com.eddyvn.laixehieuqua.simulation

import com.eddyvn.laixehieuqua.domain.CalibrationStore
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.domain.EconomyReferenceStore
import com.eddyvn.laixehieuqua.engine.DrivePipeline
import com.eddyvn.laixehieuqua.tracking.DriveStateStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SimulationController(
    private val driveStateStore:DriveStateStore,
    private val economyReferenceStore:EconomyReferenceStore,
    private val calibrationStore:CalibrationStore,
){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
    private val pipeline=DrivePipeline()
    private val mutable=MutableStateFlow(SimulationState())
    val state:StateFlow<SimulationState> = mutable

    private var job:Job?=null

    fun startScenario(scenario:SimulationScenario,speedMultiplier:Int=20){
        val frames=SimulationScenarios.frames(scenario)
        launchFrames(
            frames=frames,
            source="Scenario: ${scenario.title}",
            scenario=scenario,
            speedMultiplier=speedMultiplier,
        )
    }

    fun replay(frames:List<SimulationFrame>,speedMultiplier:Int=20){
        if(frames.isEmpty()){
            mutable.value=mutable.value.copy(
                active=false,
                paused=false,
                source="Replay",
                progress=0f,
                message="No recorded ride is available yet",
            )
            return
        }
        launchFrames(
            frames=frames,
            source="Replay: last recorded ride",
            scenario=mutable.value.scenario,
            speedMultiplier=speedMultiplier,
        )
    }

    fun togglePause(){
        if(!mutable.value.active)return
        val willPause=!mutable.value.paused
        mutable.value=mutable.value.copy(
            paused=willPause,
            message=if(willPause)"Paused" else "Running",
        )
    }

    fun setSpeedMultiplier(value:Int){
        val speed=value.coerceIn(1,100)
        mutable.value=mutable.value.copy(speedMultiplier=speed)
    }

    fun stop(){
        job?.cancel()
        job=null
        driveStateStore.markStopped()
        mutable.value=mutable.value.copy(
            active=false,
            paused=false,
            source="Idle",
            message="Stopped",
        )
    }

    private fun launchFrames(
        frames:List<SimulationFrame>,
        source:String,
        scenario:SimulationScenario,
        speedMultiplier:Int,
    ){
        job?.cancel()
        val baseTime=System.currentTimeMillis()
        pipeline.reset(sessionStartMs=baseTime)
        driveStateStore.beginSession(baseTime)
        val total=frames.lastOrNull()?.relativeTimeMs?:0L
        mutable.value=SimulationState(
            active=true,
            paused=false,
            source=source,
            scenario=scenario,
            speedMultiplier=speedMultiplier.coerceIn(1,100),
            progress=0f,
            totalScenarioMs=total,
            message="Running",
        )

        job=scope.launch{
            var previousRelative=frames.firstOrNull()?.relativeTimeMs?:0L

            for((index,frame) in frames.withIndex()){
                while(mutable.value.paused)delay(100)

                if(index>0){
                    val scenarioDelay=(frame.relativeTimeMs-previousRelative).coerceAtLeast(0L)
                    val realDelay=(scenarioDelay/mutable.value.speedMultiplier.coerceAtLeast(1))
                        .coerceAtLeast(1L)
                    delay(realDelay)
                }
                previousRelative=frame.relativeTimeMs

                if(frame.gpsAvailable){
                    val snapshot=pipeline.consume(
                        sample=DriveInputSample(
                            timestampMs=baseTime+frame.relativeTimeMs,
                            rawGpsSpeedKmh=frame.rawGpsSpeedKmh,
                            gpsAccuracyM=frame.gpsAccuracyM,
                            accelerationMs2=frame.accelerationMs2,
                            longitudinalAccelerationMs2=frame.accelerationMs2,
                            leanDeg=frame.leanDeg,
                            deltaDistanceM=frame.deltaDistanceM,
                        ),
                        reference=economyReferenceStore.state.value,
                        activeCalibration=calibrationStore.state.value,
                    )
                    driveStateStore.update(snapshot)
                }

                mutable.value=mutable.value.copy(
                    progress=if(frames.size<=1)1f else index.toFloat()/(frames.size-1),
                    elapsedScenarioMs=frame.relativeTimeMs,
                    lastOcrSpeedKmh=frame.simulatedOcrSpeedKmh,
                    message=if(frame.gpsAvailable)"Running" else "GPS dropout",
                )
            }

            driveStateStore.markStopped(baseTime+total)
            mutable.value=mutable.value.copy(
                active=false,
                paused=false,
                progress=1f,
                message="Completed",
            )
            job=null
        }
    }
}
