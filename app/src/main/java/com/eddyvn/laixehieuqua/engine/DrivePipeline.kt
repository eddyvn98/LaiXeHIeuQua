package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.ActiveCalibration
import com.eddyvn.laixehieuqua.domain.DriveInputSample
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.domain.EconomyReference

class DrivePipeline {
    private val fusion=SpeedFusionEngine()
    private val trafficDetector=TrafficDetector()
    private val eco=EcoTargetEngine()
    private val calibration=SpeedCalibrationEngine()

    private val speedHistory=ArrayDeque<Double>()
    private val targetHistory=ArrayDeque<Double>()
    private var sessionDistanceM=0.0
    private var totalDistanceM=0.0

    fun reset(sessionDistanceM:Double=0.0,totalDistanceM:Double=0.0){
        fusion.reset()
        trafficDetector.clear()
        eco.reset()
        speedHistory.clear()
        targetHistory.clear()
        this.sessionDistanceM=sessionDistanceM.coerceAtLeast(0.0)
        this.totalDistanceM=totalDistanceM.coerceAtLeast(0.0)
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
        val display=calibration.map(trueSpeed,activeCalibration?.points.orEmpty())

        val delta=sample.deltaDistanceM.coerceAtLeast(0.0)
        sessionDistanceM+=delta
        totalDistanceM+=delta

        speedHistory.addLast(trueSpeed)
        while(speedHistory.size>40)speedHistory.removeFirst()
        target.targetKmh?.let{targetHistory.addLast(it)}
        while(targetHistory.size>40)targetHistory.removeFirst()

        return DriveSnapshot(
            tracking=true,
            trueSpeedKmh=trueSpeed,
            displaySpeedKmh=display,
            rawGpsSpeedKmh=sample.rawGpsSpeedKmh,
            accelerationMs2=sample.accelerationMs2,
            leanDeg=sample.leanDeg,
            distanceKm=sessionDistanceM/1000.0,
            totalTrackedKm=totalDistanceM/1000.0,
            traffic=traffic,
            ecoTargetKmh=target.targetKmh,
            ecoTargetConfidence=target.confidence,
            speedHistoryKmh=speedHistory.toList(),
            ecoTargetHistoryKmh=targetHistory.toList(),
        )
    }
}
