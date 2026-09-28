package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.ui.components.*
import kotlin.math.*

@Composable
fun DashboardScreen(
    drive:DriveSnapshot,
    reference:EconomyReference?,
    summary:FuelSummary,
    projection:EconomyProjection?,
    template:DashboardTemplateEntity?,
    onStart:()->Unit,
    onStop:()->Unit,
){
    val accent=Color(template?.accentHex?:0xFF5CE7FF)
    val secondary=Color(template?.secondaryHex?:0xFF68FFB2)

    Column(
        Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=12.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
    ){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text(if(drive.tracking)"● LIVE" else "READY",color=if(drive.tracking)secondary else Color(0xFF708295),fontSize=11.sp)
            Text(if(drive.traffic)"TRAFFIC" else (template?.name?:"TFT Sport Bike"),color=Color(0xFF8496A7),fontSize=10.sp)
            Text("GPS "+drive.rawGpsSpeedKmh.roundToInt(),color=Color(0xFF8496A7),fontSize=10.sp)
        }

        SportSpeedGauge(drive.displaySpeedKmh,drive.ecoTargetKmh,accent,secondary,Modifier.size(310.dp))

        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            NeedleGauge(drive.accelerationMs2,0.0..4.0,"m/s²",accent,Modifier.size(130.dp))
            NeedleGauge(abs(drive.leanDeg),0.0..50.0,"LEAN °",secondary,Modifier.size(130.dp))
        }

        if(drive.traffic){
            Text("STOP/GO",color=Color(0xFFFFD166),fontWeight=FontWeight.Bold,fontSize=12.sp)
        }else drive.ecoTargetKmh?.let{target->
            Text(
                "Δ ECO "+"%+.0f".format(drive.displaySpeedKmh-target)+" km/h",
                color=secondary,
                fontWeight=FontWeight.Bold,
                fontSize=12.sp,
            )
        }

        EconomyGapChart(drive.speedHistoryKmh,drive.ecoTargetKmh,Modifier.fillMaxWidth().padding(top=6.dp))

        Row(Modifier.fillMaxWidth().padding(top=4.dp),horizontalArrangement=Arrangement.SpaceEvenly){
            Metric(summary.currentCycleKm,"CURRENT")
            Metric(projection?.projectedCycleKm?:0.0,"PROJECTED")
            Metric(reference?.cycle?.distanceKm?:summary.bestCycle?.distanceKm?:0.0,"BEST")
        }
        projection?.let{
            Text(
                (if(it.deltaToBestKm>=0)"▲ " else "▼ ")+"%+.1f km vs Best".format(it.deltaToBestKm),
                color=if(it.deltaToBestKm>=0)secondary else Color(0xFFFFD166),
                fontSize=11.sp,
                fontWeight=FontWeight.Bold,
                modifier=Modifier.padding(top=3.dp),
            )
        }

        Spacer(Modifier.weight(1f))
        Button(onClick=if(drive.tracking)onStop else onStart){
            Text(if(drive.tracking)"STOP" else "START DRIVING")
        }
    }
}

@Composable
private fun Metric(value:Double,label:String){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Text(if(value>0)"%.1f".format(value) else "—",fontSize=18.sp,fontWeight=FontWeight.Bold)
        Text(label+" KM",fontSize=8.sp,color=Color(0xFF6F8192),letterSpacing=1.sp)
    }
}
