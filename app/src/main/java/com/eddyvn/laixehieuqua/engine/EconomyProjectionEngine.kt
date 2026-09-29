package com.eddyvn.laixehieuqua.engine

import com.eddyvn.laixehieuqua.domain.*
import kotlin.math.*

class EconomyProjectionEngine {
    fun project(snapshot:DriveSnapshot,summary:FuelSummary):EconomyProjection?{
        val best=summary.bestCycle?:return null
        val target=snapshot.ecoTargetKmh
        val recent=snapshot.speedHistoryKmh.takeLast(20)

        val stabilityPenalty=if(recent.size>=5){
            val mean=recent.average()
            val variance=recent.sumOf{(it-mean).pow(2)}/recent.size
            (sqrt(variance)/20.0).coerceIn(0.0,0.08)
        }else 0.0

        val targetPenalty=if(snapshot.traffic||target==null||target<1.0) 0.0 else
            (abs(snapshot.trueSpeedKmh-target)/target*0.18).coerceIn(0.0,0.15)

        val reward=if(!snapshot.traffic&&target!=null&&abs(snapshot.trueSpeedKmh-target)<=2.0&&stabilityPenalty<0.02) 0.02 else 0.0
        val factor=(1.0-targetPenalty-stabilityPenalty+reward).coerceIn(0.75,1.08)
        val behavioral=best.distanceKm*factor
        val projected=max(summary.currentCycleKm,behavioral)

        return EconomyProjection(
            projectedCycleKm=projected,
            deltaToBestKm=projected-best.distanceKm,
            behaviorFactor=factor,
        )
    }
}
