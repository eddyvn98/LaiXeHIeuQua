package com.eddyvn.laixehieuqua.engine

import kotlin.math.abs

class SpeedFusionEngine{
    private var lastSpeed=0.0
    private var lastTime:Long?=null
    fun update(timeMs:Long,gpsSpeedKmh:Double,accelerationMs2:Double,gpsAccuracyM:Float):Double{
        val previous=lastTime
        if(previous==null){lastTime=timeMs;lastSpeed=gpsSpeedKmh.coerceAtLeast(0.0);return lastSpeed}
        val dt=((timeMs-previous).coerceIn(100,3000))/1000.0
        val predicted=(lastSpeed+accelerationMs2*3.6*dt).coerceAtLeast(0.0)
        val weight=when{gpsAccuracyM<=6f->0.72;gpsAccuracyM<=15f->0.58;else->0.38}
        val adaptive=if(abs(gpsSpeedKmh-predicted)>20.0) weight*0.65 else weight
        lastSpeed=(adaptive*gpsSpeedKmh+(1.0-adaptive)*predicted).coerceIn(0.0,180.0)
        lastTime=timeMs
        return lastSpeed
    }
    fun reset(){lastSpeed=0.0;lastTime=null}
}
