package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.domain.FuelSummary
import com.eddyvn.laixehieuqua.ui.components.EconomyGapChart
import com.eddyvn.laixehieuqua.ui.components.SportSpeedGauge
import com.eddyvn.laixehieuqua.ui.components.SpeedTrendChart
import kotlin.math.roundToInt

@Composable
internal fun LandscapeDashboardContent(
    drive:DriveSnapshot,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    sessionElapsedMs:Long,
    fuelSummary:FuelSummary,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    statusLabel:String,
    gaugeActionLabel:String,
    gaugeActionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    speedAccent:Color,
    onGaugeClick:()->Unit,
    maxWidth:Dp,
    maxHeight:Dp,
){
    val gaugeSize=minOf(280.dp,maxHeight*.78f,maxWidth*.38f)
    val efficiency=drive.efficiencyHistory.lastOrNull()?.roundToInt()?.toString()?:"—"

    Column(Modifier.fillMaxSize().padding(horizontal=14.dp,vertical=6.dp)){
        LandscapeHeader(statusLabel,secondary,onFuelClick)

        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement=Arrangement.spacedBy(16.dp),
            verticalAlignment=Alignment.CenterVertically,
        ){
            Column(
                modifier=Modifier.width(gaugeSize+24.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center,
            ){
                SportSpeedGauge(
                    speedKmh=drive.displaySpeedKmh,
                    averageSpeedKmh=drive.averageSpeedKmh,
                    ecoTargetKmh=drive.ecoTargetKmh,
                    accent=speedAccent,
                    secondary=secondary,
                    modifier=Modifier.size(gaugeSize),
                    actionLabel=gaugeActionLabel,
                    clickEnabled=gaugeActionEnabled,
                    onClick=onGaugeClick,
                )
                SpeedTrendChart(
                    speeds=drive.speedHistoryKmh,
                    targetKmh=drive.ecoTargetKmh,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=30.dp,
                )
            }

            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement=Arrangement.SpaceEvenly,
            ){
                LandscapePrimaryMetrics(
                    acceleration=drive.accelerationMs2,
                    averageAcceleration=drive.averageAccelerationMs2,
                    slope=drive.leanDeg,
                    accent=accent,
                    secondary=secondary,
                    maxGaugeSize=maxHeight*.27f,
                )
                LandscapeTripMetrics(
                    vehicleOdometerKm=vehicleOdometerKm,
                    tripMeterKm=tripMeterKm,
                    sessionElapsedMs=sessionElapsedMs,
                    efficiency=efficiency,
                    onResetTrip=onResetTrip,
                    onOdoClick=onOdoClick,
                )
                LandscapeFuelMetrics(fuelSummary)
                EconomyGapChart(
                    efficiency=drive.efficiencyHistory,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=28.dp,
                    compactAxisLabels=true,
                )
            }
        }
    }
}
