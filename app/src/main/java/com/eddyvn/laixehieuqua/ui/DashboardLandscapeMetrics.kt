package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.GasStation
import com.eddyvn.laixehieuqua.domain.FuelSummary
import com.eddyvn.laixehieuqua.ui.components.RunningAccelerationGauge
import com.eddyvn.laixehieuqua.ui.components.RunningClockGauge
import com.eddyvn.laixehieuqua.ui.components.RunningSlopeGauge
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun LandscapeHeader(
    statusLabel:String,
    secondary:Color,
    onFuelClick:()->Unit,
){
    Row(
        Modifier.fillMaxWidth().padding(end=52.dp),
        horizontalArrangement=Arrangement.SpaceBetween,
        verticalAlignment=Alignment.CenterVertically,
    ){
        Text(
            statusLabel,
            color=secondary,
            fontSize=12.sp,
            fontWeight=FontWeight.Bold,
        )
        IconButton(onClick=onFuelClick,modifier=Modifier.size(44.dp)){
            Icon(
                Tabler.Outline.GasStation,
                contentDescription="Đổ xăng",
                tint=secondary,
                modifier=Modifier.size(28.dp),
            )
        }
    }
}

@Composable
internal fun LandscapePrimaryMetrics(
    acceleration:Double,
    slope:Double,
    accent:Color,
    secondary:Color,
    maxGaugeSize:Dp,
){
    Row(
        Modifier.fillMaxWidth().heightIn(max=maxGaugeSize),
        horizontalArrangement=Arrangement.SpaceEvenly,
        verticalAlignment=Alignment.CenterVertically,
    ){
        RunningClockGauge(
            accent=accent,
            modifier=Modifier.weight(1f).fillMaxHeight(),
        )
        RunningAccelerationGauge(
            value=acceleration,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).fillMaxHeight(),
        )
        RunningSlopeGauge(
            value=slope,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LandscapeTripMetrics(
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    averageSpeedKmh:Double,
    sessionElapsedMs:Long,
    efficiency:String,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.SpaceEvenly,
        verticalAlignment=Alignment.CenterVertically,
    ){
        ReadableMetric(
            vehicleOdometerKm?.let{String.format(Locale.US,"%.1f",it)}?:"—",
            "ODO km",
            Color.White,
            Modifier.weight(1f).combinedClickable(onClick=onOdoClick),
        )
        ReadableMetric(
            String.format(Locale.US,"%.1f",tripMeterKm),
            "TRIP km",
            Color.White,
            Modifier.weight(1f).combinedClickable(onClick={},onLongClick=onResetTrip),
        )
        ReadableMetric(
            String.format(Locale.US,"%.0f",averageSpeedKmh),
            "AVG km/h",
            Color.White,
            Modifier.weight(1f),
        )
        ReadableMetric(
            formatShortDuration(sessionElapsedMs),
            "TIME",
            Color.White,
            Modifier.weight(1f),
        )
        ReadableMetric(efficiency,"ECO",Color.White,Modifier.weight(1f))
    }
}

@Composable
internal fun LandscapeFuelMetrics(summary:FuelSummary){
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF34333B),RoundedCornerShape(16.dp))
            .padding(horizontal=8.dp,vertical=6.dp),
        horizontalArrangement=Arrangement.SpaceEvenly,
        verticalAlignment=Alignment.CenterVertically,
    ){
        ReadableMetric(summary.estimatedRemainingLiters?.let{"%.1f".format(Locale.US,it)}?:"—","FUEL L",Color.White,Modifier.weight(1f))
        ReadableMetric(summary.estimatedRangeKm?.let{"%.0f".format(Locale.US,it)}?:"—","RANGE km",Color.White,Modifier.weight(1f))
        ReadableMetric(summary.averageLitersPer100Km?.let{"%.1f".format(Locale.US,it)}?:"—","L/100",Color.White,Modifier.weight(1f))
        ReadableMetric(summary.averageKmPerLiter?.let{"%.1f".format(Locale.US,it)}?:"—","KM/L",Color.White,Modifier.weight(1f))
    }
}

@Composable
internal fun ReadableMetric(
    value:String,
    label:String,
    color:Color,
    modifier:Modifier=Modifier,
){
    Column(
        modifier=modifier.padding(horizontal=3.dp,vertical=4.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center,
    ){
        Text(
            value,
            color=color,
            fontSize=28.sp,
            fontWeight=FontWeight.Bold,
            maxLines=1,
        )
        Text(
            label,
            color=Color(0xFFB9C9D4),
            fontSize=12.sp,
            fontWeight=FontWeight.Medium,
            maxLines=1,
        )
    }
}

@Composable
private fun formatShortDuration(elapsedMs:Long):String{
    val totalSeconds=elapsedMs.coerceAtLeast(0L)/1_000L
    val hours=totalSeconds/3_600L
    val minutes=(totalSeconds%3_600L)/60L
    val seconds=totalSeconds%60L
    return "%02d:%02d:%02d".format(hours,minutes,seconds)
}
