package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.ActiveCalibration
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.domain.EconomyReference
import kotlin.math.abs

class DrivePipeline {
    private val fusion=SpeedFusionEngine()
    private val trafficDetector=TrafficDetector()
    private val eco=EcoTargetEngine()

    private val speedHistory=ArrayDeque<Double>()
    private val targetHistory=ArrayDeque<Double>()
    private val efficiencyHistory=ArrayDeque<Double>()
    private var sessionDistanceM=0.0
    private var totalDistanceM=0.0
    private var movingTimeMs=0L
    private var lastSampleTimeMs:Long?=null

    fun reset(sessionDistanceM:Double=0.0,totalDistanceM:Double=0.0){
        fusion.reset()
        trafficDetector.clear()
        eco.reset()
        speedHistory.clear()
        targetHistory.clear()
        efficiencyHistory.clear()
        this.sessionDistanceM=sessionDistanceM.coerceAtLeast(0.0)
        this.totalDistanceM=totalDistanceM.coerceAtLeast(0.0)
        movingTimeMs=0L
        lastSampleTimeMs=null
    }

    fun consume(
        sample:DriveInputSample,
        reference:EconomyReference?,
        activeCalibration:ActiveCalibration?,
    ):DriveSnapshot{
        val trueSpeed=fusion.update(
            sample.timestampMs,
            sample.rawGpsSpeedKmh,
            sample.accelerationMs2,
            sample.gpsAccuracyM,
        )
        val traffic=trafficDetector.update(sample.timestampMs,trueSpeed)
        val target=eco.update(
            trueSpeed,
            sample.accelerationMs2,
            traffic,
            reference?.ecoSpeedKmh,
        )

        // Camera/speedometer calibration is intentionally not applied to the live
        // speed display anymore. GPS + sensor fusion is the single speed source.
        @Suppress("UNUSED_VARIABLE")
        val ignoredCalibration=activeCalibration
        val display=trueSpeed

        val previousTime=lastSampleTimeMs
        if(previousTime!=null && trueSpeed>2.0){
            movingTimeMs+=(sample.timestampMs-previousTime).coerceIn(0L,3_000L)
        }
        lastSampleTimeMs=sample.timestampMs

        val delta=sample.deltaDistanceM.coerceAtLeast(0.0)
        sessionDistanceM+=delta
        totalDistanceM+=delta

        val targetGap=target.targetKmh?.let{abs(trueSpeed-it)}?:0.0
        val accelPenalty=abs(sample.accelerationMs2).coerceAtMost(5.0)*7.0
        val targetPenalty=if(traffic)0.0 else targetGap.coerceAtMost(25.0)*1.1
        val efficiency=(100.0-accelPenalty-targetPenalty).coerceIn(0.0,100.0)

        speedHistory.addLast(trueSpeed)
        while(speedHistory.size>50)speedHistory.removeFirst()
        target.targetKmh?.let{targetHistory.addLast(it)}
        while(targetHistory.size>50)targetHistory.removeFirst()
        efficiencyHistory.addLast(efficiency)
        while(efficiencyHistory.size>50)efficiencyHistory.removeFirst()

        val distanceKm=sessionDistanceM/1000.0
        val averageSpeed=if(movingTimeMs>0L){
            distanceKm/(movingTimeMs/3_600_000.0)
        }else 0.0

        return DriveSnapshot(
            tracking=true,
            trueSpeedKmh=trueSpeed,
            displaySpeedKmh=display,
            rawGpsSpeedKmh=sample.rawGpsSpeedKmh,
            accelerationMs2=sample.accelerationMs2,
            leanDeg=sample.leanDeg,
            distanceKm=distanceKm,
            totalTrackedKm=totalDistanceM/1000.0,
            movingTimeMs=movingTimeMs,
            averageSpeedKmh=averageSpeed.coerceIn(0.0,180.0),
            traffic=traffic,
            ecoTargetKmh=target.targetKmh,
            ecoTargetConfidence=target.confidence,
            speedHistoryKmh=speedHistory.toList(),
            ecoTargetHistoryKmh=targetHistory.toList(),
            efficiencyHistory=efficiencyHistory.toList(),
        )
    }
}
