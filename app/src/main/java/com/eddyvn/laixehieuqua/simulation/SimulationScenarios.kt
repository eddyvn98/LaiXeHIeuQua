package com.eddyvn.laixehieuqua.simulation

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

object SimulationScenarios {
    fun frames(scenario:SimulationScenario):List<SimulationFrame> = when(scenario){
        SimulationScenario.CITY_TRAFFIC -> cityTraffic()
        SimulationScenario.CLEAR_ROAD -> clearRoad()
        SimulationScenario.ACCEL_BRAKE -> accelBrake()
        SimulationScenario.GPS_STRESS -> gpsStress()
        SimulationScenario.LEANING -> leaning()
        SimulationScenario.CALIBRATION -> calibration()
        SimulationScenario.LONG_RIDE -> longRide()
    }

    private fun cityTraffic():List<SimulationFrame>{
        val pattern=listOf(
            0.0,0.0,0.0,4.0,9.0,15.0,20.0,16.0,10.0,4.0,
            0.0,0.0,6.0,12.0,18.0,14.0,8.0,2.0,0.0,0.0,
            5.0,11.0,17.0,22.0,15.0,7.0,0.0,0.0,0.0,0.0,
        )
        val speeds=List(150){pattern[it%pattern.size]}
        return framesFromSpeeds(speeds){index,speed->
            val lean=if(speed>10.0 && index%24 in 10..14)8.0 else 0.0
            FrameExtras(accuracyM=7f,leanDeg=lean)
        }
    }

    private fun clearRoad():List<SimulationFrame>{
        val speeds=List(181){i->50.0+listOf(-1.2,-0.6,0.0,0.5,1.0,0.3)[i%6]}
        return framesFromSpeeds(speeds){_,_->FrameExtras(accuracyM=4f)}
    }

    private fun accelBrake():List<SimulationFrame>{
        val speeds=List(181){i->
            when(val phase=i%60){
                in 0..20 -> phase*2.5
                in 21..40 -> 50.0
                else -> ((60-phase)*2.5).coerceAtLeast(0.0)
            }
        }
        return framesFromSpeeds(speeds){_,_->FrameExtras(accuracyM=5f)}
    }

    private fun gpsStress():List<SimulationFrame>{
        val out=mutableListOf<SimulationFrame>()
        var previousSpeed=45.0
        var pendingDistance=0.0
        for(i in 0..180){
            val unavailable=i in 55..62 || i in 118..126
            val accuracy=when{
                unavailable -> 120f
                i in 35..45 || i in 95..105 -> 45f
                else -> 6f
            }
            val spike=when(i){
                40,99,145 -> 24.0
                42,101,147 -> -20.0
                else -> listOf(-1.5,-0.5,0.0,0.7,1.2)[i%5]
            }
            val speed=(45.0+spike).coerceAtLeast(0.0)
            val distance=45.0/3.6
            pendingDistance+=distance
            val delta=if(unavailable)0.0 else pendingDistance.also{pendingDistance=0.0}
            val acceleration=(speed-previousSpeed)/3.6
            out+=SimulationFrame(
                relativeTimeMs=i*1_000L,
                rawGpsSpeedKmh=speed,
                gpsAccuracyM=accuracy,
                accelerationMs2=acceleration,
                deltaDistanceM=delta,
                gpsAvailable=!unavailable,
            )
            previousSpeed=speed
        }
        return out
    }

    private fun leaning():List<SimulationFrame>{
        val speeds=List(181){38.0+listOf(-0.5,0.0,0.4)[it%3]}
        return framesFromSpeeds(speeds){index,_->
            val lean=when(index%60){
                in 10..22 -> -28.0*sin((index%60-10)/12.0)
                in 35..47 -> 30.0*sin((index%60-35)/12.0)
                else -> 0.0
            }
            FrameExtras(accuracyM=5f,leanDeg=lean)
        }
    }

    private fun calibration():List<SimulationFrame>{
        val speeds=List(180){i->
            when(i/60){0->30.0;1->50.0;else->70.0}
        }
        return framesFromSpeeds(speeds){_,speed->
            FrameExtras(
                accuracyM=4f,
                ocr=(speed*1.04+1.0).roundToInt(),
            )
        }
    }

    private fun longRide():List<SimulationFrame>{
        val out=ArrayList<SimulationFrame>(7_201)
        var previous=0.0
        for(i in 0..7_200){
            val minute=(i/60)%30
            val speed=when{
                minute<4 -> (15.0+(i%20)*1.4).coerceAtMost(42.0)
                minute<8 -> 48.0+sin(i/12.0)*2.0
                minute<12 -> if(i%35<8)5.0 else 28.0+sin(i/8.0)*6.0
                minute<20 -> 55.0+sin(i/18.0)*3.0
                minute<24 -> 38.0+sin(i/9.0)*5.0
                else -> 50.0+sin(i/15.0)*2.0
            }.coerceAtLeast(0.0)
            val accuracy=if(i%900 in 400..420)35f else 6f
            val acceleration=((speed-previous)/3.6).coerceIn(-4.0,4.0)
            val lean=if(i%90 in 25..38)12.0*sin((i%90-25)/13.0) else 0.0
            out+=SimulationFrame(
                relativeTimeMs=i*1_000L,
                rawGpsSpeedKmh=speed,
                gpsAccuracyM=accuracy,
                accelerationMs2=acceleration,
                leanDeg=lean,
                deltaDistanceM=speed/3.6,
            )
            previous=speed
        }
        return out
    }

    private data class FrameExtras(
        val accuracyM:Float=5f,
        val leanDeg:Double=0.0,
        val ocr:Int?=null,
    )

    private fun framesFromSpeeds(
        speeds:List<Double>,
        extras:(Int,Double)->FrameExtras,
    ):List<SimulationFrame>{
        var previous=speeds.firstOrNull()?:0.0
        return speeds.mapIndexed{index,speed->
            val extra=extras(index,speed)
            val acceleration=((speed-previous)/3.6).let{
                if(abs(it)<0.02)0.0 else it.coerceIn(-5.0,5.0)
            }
            previous=speed
            SimulationFrame(
                relativeTimeMs=index*1_000L,
                rawGpsSpeedKmh=speed,
                gpsAccuracyM=extra.accuracyM,
                accelerationMs2=acceleration,
                leanDeg=extra.leanDeg,
                deltaDistanceM=speed/3.6,
                simulatedOcrSpeedKmh=extra.ocr,
            )
        }
    }
}
