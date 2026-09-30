package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.CalibrationPoint

class SpeedCalibrationEngine{
    fun map(trueSpeedKmh:Double,rawPoints:List<CalibrationPoint>):Double{
        val points=rawPoints
            .sortedBy{it.trueSpeedKmh}
            .distinctBy{it.trueSpeedKmh.toInt()}
        if(points.isEmpty())return trueSpeedKmh
        if(points.size==1){
            val correction=(points[0].vehicleSpeedKmh-points[0].trueSpeedKmh).coerceIn(-10.0,10.0)
            return (trueSpeedKmh+correction).coerceAtLeast(0.0)
        }

        if(trueSpeedKmh<=points.first().trueSpeedKmh){
            val correction=(points.first().vehicleSpeedKmh-points.first().trueSpeedKmh).coerceIn(-10.0,10.0)
            return (trueSpeedKmh+correction).coerceAtLeast(0.0)
        }
        if(trueSpeedKmh>=points.last().trueSpeedKmh){
            val correction=(points.last().vehicleSpeedKmh-points.last().trueSpeedKmh).coerceIn(-10.0,10.0)
            return (trueSpeedKmh+correction).coerceAtLeast(0.0)
        }

        val i=points.indexOfLast{it.trueSpeedKmh<=trueSpeedKmh}
        val a=points[i]
        val b=points[i+1]
        val dx=(b.trueSpeedKmh-a.trueSpeedKmh).coerceAtLeast(1.0)
        val t=((trueSpeedKmh-a.trueSpeedKmh)/dx).coerceIn(0.0,1.0)
        val correctionA=(a.vehicleSpeedKmh-a.trueSpeedKmh).coerceIn(-10.0,10.0)
        val correctionB=(b.vehicleSpeedKmh-b.trueSpeedKmh).coerceIn(-10.0,10.0)
        val correction=correctionA+t*(correctionB-correctionA)
        return (trueSpeedKmh+correction).coerceAtLeast(0.0)
    }

    fun isMonotonic(points:List<CalibrationPoint>)=
        points.sortedBy{it.trueSpeedKmh}.zipWithNext().all{(a,b)->b.vehicleSpeedKmh>=a.vehicleSpeedKmh}
}
