package com.eddyvn.laixehieuqua.engine

import java.util.ArrayDeque
import kotlin.math.abs

data class EcoTargetResult(val targetKmh:Double?,val confidence:Double)
class EcoTargetEngine{
    private val stable=ArrayDeque<Double>()
    fun update(speedKmh:Double,accelerationMs2:Double,traffic:Boolean,bestReferenceSpeedKmh:Double?):EcoTargetResult{
        if(traffic||speedKmh<12.0){stable.clear();return EcoTargetResult(null,0.0)}
        if(abs(accelerationMs2)<=0.65){stable.addLast(speedKmh);while(stable.size>30)stable.removeFirst()}
        val median=stable.takeIf{it.size>=6}?.sorted()?.let{it[it.size/2]}
        val target=when{
            bestReferenceSpeedKmh!=null&&median!=null->0.70*bestReferenceSpeedKmh+0.30*median
            bestReferenceSpeedKmh!=null->bestReferenceSpeedKmh
            else->median
        }?.coerceIn(15.0,90.0)
        val confidence=when{
            target==null->0.0
            bestReferenceSpeedKmh!=null&&stable.size>=10->0.95
            bestReferenceSpeedKmh!=null->0.80
            stable.size>=15->0.70
            else->0.50
        }
        return EcoTargetResult(target,confidence)
    }
    fun reset()=stable.clear()
}
