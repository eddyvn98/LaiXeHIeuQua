package com.eddyvn.laixehieuqua.engine

import java.util.ArrayDeque

class TrafficDetector(private val windowMs:Long=30_000){
    private data class Sample(val timeMs:Long,val speedKmh:Double)
    private val samples=ArrayDeque<Sample>()
    fun update(timeMs:Long,speedKmh:Double):Boolean{
        samples.addLast(Sample(timeMs,speedKmh.coerceAtLeast(0.0)))
        while(samples.isNotEmpty() && timeMs-samples.first.timeMs>windowMs) samples.removeFirst()
        if(samples.size<8) return false
        val speeds=samples.map{it.speedKmh}
        val median=speeds.sorted()[speeds.size/2]
        val stoppedRatio=speeds.count{it<2.0}.toDouble()/speeds.size
        val transitions=speeds.zipWithNext().count{(a,b)->a>6.0 && b<2.0}
        return median<18.0 && (stoppedRatio>=0.20 || transitions>=2)
    }
    fun clear()=samples.clear()
}
