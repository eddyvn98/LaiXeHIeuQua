package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.CalibrationPoint

class SpeedCalibrationEngine{
    fun map(trueSpeedKmh:Double,rawPoints:List<CalibrationPoint>):Double{
        val points=rawPoints.sortedBy{it.trueSpeedKmh}.distinctBy{it.trueSpeedKmh}
        if(points.isEmpty())return trueSpeedKmh
        if(points.size==1)return (trueSpeedKmh+points[0].vehicleSpeedKmh-points[0].trueSpeedKmh).coerceAtLeast(0.0)
        val i=points.indexOfLast{it.trueSpeedKmh<=trueSpeedKmh}
        val pair=when{ i<0->points[0] to points[1]; i>=points.lastIndex->points[points.lastIndex-1] to points.last(); else->points[i] to points[i+1] }
        val (a,b)=pair
        val dx=b.trueSpeedKmh-a.trueSpeedKmh
        if(dx==0.0)return a.vehicleSpeedKmh
        val t=(trueSpeedKmh-a.trueSpeedKmh)/dx
        return (a.vehicleSpeedKmh+t*(b.vehicleSpeedKmh-a.vehicleSpeedKmh)).coerceAtLeast(0.0)
    }
    fun isMonotonic(points:List<CalibrationPoint>)=
        points.sortedBy{it.trueSpeedKmh}.zipWithNext().all{(a,b)->b.vehicleSpeedKmh>=a.vehicleSpeedKmh}
}
