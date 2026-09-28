package com.eddyvn.laixehieuqua.camera

import com.eddyvn.laixehieuqua.domain.CalibrationPoint
import kotlin.math.abs

class CalibrationSampleCollector{
    private data class Sample(val timeMs:Long,val trueSpeed:Double,val vehicleSpeed:Double)
    private val samples=ArrayDeque<Sample>()
    private var lastAcceptedMs=0L

    fun offer(timeMs:Long,trueSpeedKmh:Double,vehicleSpeedKmh:Double):CalibrationPoint?{
        if(trueSpeedKmh<10.0||vehicleSpeedKmh<10.0)return null
        samples.addLast(Sample(timeMs,trueSpeedKmh,vehicleSpeedKmh))
        while(samples.size>5)samples.removeFirst()
        if(samples.size<4||timeMs-lastAcceptedMs<4_000)return null
        val recent=samples.takeLast(4)
        val vehicleSpread=recent.maxOf{it.vehicleSpeed}-recent.minOf{it.vehicleSpeed}
        val trueSpread=recent.maxOf{it.trueSpeed}-recent.minOf{it.trueSpeed}
        if(vehicleSpread>1.0||trueSpread>1.5)return null
        val trueAvg=recent.map{it.trueSpeed}.average()
        val vehicleAvg=recent.map{it.vehicleSpeed}.average()
        if(abs(trueAvg-vehicleAvg)>25.0)return null
        lastAcceptedMs=timeMs;samples.clear()
        return CalibrationPoint(trueAvg,vehicleAvg)
    }
}
