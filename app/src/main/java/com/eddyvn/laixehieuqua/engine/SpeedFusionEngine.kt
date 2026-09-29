package com.eddyvn.laixehieuqua.engine

import kotlin.math.abs

class SpeedFusionEngine {
    private var lastSpeed=0.0
    private var lastTime:Long?=null

    fun update(timeMs:Long,gpsSpeedKmh:Double,accelerationMs2:Double,gpsAccuracyM:Float):Double{
        val previous=lastTime
        if(previous==null){
            lastTime=timeMs
            lastSpeed=gpsSpeedKmh.coerceAtLeast(0.0)
            return lastSpeed
        }

        val dt=((timeMs-previous).coerceIn(100,3000))/1000.0
        val sensorMagnitude=abs(accelerationMs2).coerceAtMost(5.0)
        val signedAcceleration=when{
            gpsSpeedKmh>lastSpeed+0.5 -> sensorMagnitude
            gpsSpeedKmh<lastSpeed-0.5 -> -sensorMagnitude
            else -> 0.0
        }
        val predicted=(lastSpeed+signedAcceleration*3.6*dt).coerceAtLeast(0.0)
        val baseWeight=when{
            gpsAccuracyM<=6f -> 0.74
            gpsAccuracyM<=15f -> 0.60
            else -> 0.42
        }
        val disagreement=abs(gpsSpeedKmh-predicted)
        val gpsWeight=if(disagreement>20.0)baseWeight*0.70 else baseWeight

        lastSpeed=(gpsWeight*gpsSpeedKmh+(1.0-gpsWeight)*predicted).coerceIn(0.0,180.0)
        lastTime=timeMs
        return lastSpeed
    }

    fun reset(){lastSpeed=0.0;lastTime=null}
}
