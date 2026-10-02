package com.eddyvn.laixehieuqua.engine

import kotlin.math.abs

class SpeedFusionEngine {
    private var lastSpeed=0.0
    private var lastTime:Long?=null
    private var stationarySamples=0

    fun update(timeMs:Long,gpsSpeedKmh:Double,accelerationMs2:Double,gpsAccuracyM:Float):Double{
        val gps=gpsSpeedKmh.coerceAtLeast(0.0)

        if(gps<=1.2 && gpsAccuracyM<=25f){
            stationarySamples++
        }else{
            stationarySamples=0
        }

        // Hard zero-velocity correction: never let accelerometer integration create
        // phantom speed while GPS repeatedly reports that the vehicle is stopped.
        if(stationarySamples>=2){
            lastTime=timeMs
            lastSpeed=0.0
            return 0.0
        }

        val previous=lastTime
        if(previous==null){
            lastTime=timeMs
            lastSpeed=if(gps<1.0)0.0 else gps
            return lastSpeed
        }

        val dt=((timeMs-previous).coerceIn(100,2500))/1000.0
        val projected=accelerationMs2.coerceIn(-3.5,3.5)
        val signedAcceleration=when{
            gps>lastSpeed+0.7 -> projected.coerceAtLeast(0.0)
            gps<lastSpeed-0.7 -> projected.coerceAtMost(0.0)
            else -> 0.0
        }
        val predicted=(lastSpeed+signedAcceleration*3.6*dt).coerceAtLeast(0.0)

        val baseWeight=when{
            gpsAccuracyM<=6f -> 0.82
            gpsAccuracyM<=15f -> 0.72
            gpsAccuracyM<=25f -> 0.60
            else -> 0.48
        }
        val disagreement=abs(gps-predicted)
        val gpsWeight=if(disagreement>18.0)baseWeight*0.78 else baseWeight
        val fused=gpsWeight*gps+(1.0-gpsWeight)*predicted

        // Small dead-zone removes 0.x/1.x km/h GPS wander at a stop.
        lastSpeed=if(gps<1.8 && fused<2.2)0.0 else fused.coerceIn(0.0,180.0)
        lastTime=timeMs
        return lastSpeed
    }

    fun reset(){
        lastSpeed=0.0
        lastTime=null
        stationarySamples=0
    }
}
