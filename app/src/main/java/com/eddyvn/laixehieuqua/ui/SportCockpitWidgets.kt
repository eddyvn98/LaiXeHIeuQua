package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eddyvn.laixehieuqua.data.WeatherState
import com.eddyvn.laixehieuqua.domain.FuelSummary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

@Composable
internal fun SportStatusBar(
    weather:WeatherState,
    statusLabel:String,
    accent:Color,
    onRefreshWeather:()->Unit,
){
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}
    LaunchedEffect(Unit){while(true){delay(60_000);now=System.currentTimeMillis()}}
    val locale=remember{Locale.forLanguageTag("vi-VN")}
    val date=remember(now){SimpleDateFormat("EEE dd/MM",locale).format(Date(now)).uppercase(locale)}
    val weatherText=weather.currentTempC?.let{
        val rain=weather.todayRainChance?.let{p->" · MƯA $p%"}?:""
        "${it.roundToInt()}°C$rain"
    }?:if(weather.loading)"ĐANG TẢI..." else "THỜI TIẾT —"

    Row(
        Modifier.fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF111318),Color(0xFF20242B),Color(0xFF111318))),
                RoundedCornerShape(14.dp),
            )
            .border(1.dp,accent.copy(alpha=.45f),RoundedCornerShape(14.dp))
            .padding(horizontal=12.dp,vertical=8.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.SpaceBetween,
    ){
        Text(date,color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Black)
        Text(statusLabel,color=accent,fontSize=13.sp,fontWeight=FontWeight.Black,maxLines=1)
        Text(
            weatherText,
            color=Color.White,
            fontSize=14.sp,
            fontWeight=FontWeight.Bold,
            modifier=Modifier.clickable(onClick=onRefreshWeather),
        )
    }
}

@Composable
internal fun SportSpeedCluster(
    speedKmh:Double,
    averageSpeedKmh:Double,
    ecoTargetKmh:Double?,
    accent:Color,
    secondary:Color,
    actionLabel:String,
    enabled:Boolean,
    modifier:Modifier=Modifier,
    onClick:()->Unit,
){
    Box(
        modifier.clickable(enabled=enabled,onClick=onClick),
        contentAlignment=Alignment.Center,
    ){
        Canvas(Modifier.fillMaxSize()){
            val c=Offset(size.width/2,size.height/2)
            val r=size.minDimension*.43f
            drawCircle(Color(0xFF050607),r*1.07f,c)
            drawCircle(Color(0xFF1D2228),r*1.02f,c,style=Stroke(12.dp.toPx()))
            drawCircle(Color(0xFF59616B),r*.98f,c,style=Stroke(1.dp.toPx()))

            for(i in 0..24){
                val a=Math.toRadians((135.0+270.0*i/24.0))
                val major=i%4==0
                val outer=r*.94f
                val inner=outer-(if(major)18.dp.toPx() else 9.dp.toPx())
                val p1=Offset(c.x+inner*cos(a).toFloat(),c.y+inner*sin(a).toFloat())
                val p2=Offset(c.x+outer*cos(a).toFloat(),c.y+outer*sin(a).toFloat())
                drawLine(if(major)Color.White else Color(0xFF68717A),p1,p2,if(major)3.dp.toPx() else 1.2.dp.toPx())
            }

            val arcR=r*.82f
            val box=androidx.compose.ui.geometry.Size(arcR*2,arcR*2)
            val top=Offset(c.x-arcR,c.y-arcR)
            drawArc(Color(0xFF252A30),135f,270f,false,top,box,style=Stroke(12.dp.toPx(),cap=StrokeCap.Round))
            val progress=(speedKmh/120.0).toFloat().coerceIn(0f,1f)
            if(progress>0f){
                drawArc(accent,135f,270f*progress,false,top,box,style=Stroke(9.dp.toPx(),cap=StrokeCap.Round))
                drawArc(accent.copy(alpha=.18f),135f,270f*progress,false,top,box,style=Stroke(20.dp.toPx(),cap=StrokeCap.Round))
            }
            ecoTargetKmh?.let{
                val a=Math.toRadians(135.0+270.0*(it/120.0).coerceIn(0.0,1.0))
                val p1=Offset(c.x+r*.70f*cos(a).toFloat(),c.y+r*.70f*sin(a).toFloat())
                val p2=Offset(c.x+r*.90f*cos(a).toFloat(),c.y+r*.90f*sin(a).toFloat())
                drawLine(secondary,p1,p2,5.dp.toPx(),StrokeCap.Round)
            }
        }

        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text("SPORT",color=accent,fontSize=13.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp)
            Text(
                speedKmh.roundToInt().toString(),
                color=Color.White,
                fontSize=72.sp,
                fontWeight=FontWeight.Black,
                lineHeight=72.sp,
            )
            Text("KM/H",color=Color(0xFFE4E8EC),fontSize=16.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp)
            Text(
                "AVG ${averageSpeedKmh.roundToInt()}  ·  ECO ${ecoTargetKmh?.roundToInt()?:"—"}",
                color=secondary,
                fontSize=13.sp,
                fontWeight=FontWeight.Bold,
                modifier=Modifier.padding(top=7.dp),
            )
            Text(actionLabel,color=Color(0xFFB9C0C8),fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
        }
    }
}

@Composable
internal fun SportTelemetryPod(
    value:String,
    label:String,
    accent:Color,
    modifier:Modifier=Modifier,
){
    Box(
        modifier
            .background(
                Brush.verticalGradient(listOf(Color(0xFF24282E),Color(0xFF0B0D10))),
                RoundedCornerShape(18.dp),
            )
            .border(1.dp,Color(0xFF4A515A),RoundedCornerShape(18.dp))
            .padding(vertical=9.dp,horizontal=6.dp),
        contentAlignment=Alignment.Center,
    ){
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Box(Modifier.width(34.dp).height(3.dp).background(accent,RoundedCornerShape(3.dp)))
            Text(value,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black,modifier=Modifier.padding(top=6.dp),maxLines=1)
            Text(label,color=Color(0xFFD4D9DE),fontSize=11.sp,fontWeight=FontWeight.Bold,maxLines=1)
        }
    }
}

@Composable
internal fun SportFuelStrip(
    summary:FuelSummary,
    accent:Color,
    modifier:Modifier=Modifier,
){
    val remaining=summary.estimatedRemainingLiters
    val capacity=summary.tankCapacityLiters
    val fraction=if(remaining!=null&&capacity!=null&&capacity>0) (remaining/capacity).coerceIn(0.0,1.0) else 0.0
    val filled=(fraction*8).roundToInt().coerceIn(0,8)
    Row(
        modifier
            .background(Color(0xFF111419),RoundedCornerShape(16.dp))
            .border(1.dp,Color(0xFF3A4149),RoundedCornerShape(16.dp))
            .padding(horizontal=12.dp,vertical=10.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(10.dp),
    ){
        Text("FUEL",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Black)
        Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(3.dp)){
            repeat(8){i->
                Box(
                    Modifier.weight(1f).height(12.dp)
                        .background(if(i<filled)accent else Color(0xFF343A42),RoundedCornerShape(3.dp))
                )
            }
        }
        Text(
            summary.estimatedRangeKm?.let{"${it.roundToInt()} KM"}?:"— KM",
            color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Black,
        )
    }
}

@Composable
internal fun SportDataStrip(
    odo:Double?,
    trip:Double,
    elapsed:String,
    accent:Color,
    modifier:Modifier=Modifier,
){
    Row(
        modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF0A0B0D),Color(0xFF1C2025),Color(0xFF0A0B0D))),
                RoundedCornerShape(14.dp),
            )
            .border(1.dp,accent.copy(alpha=.35f),RoundedCornerShape(14.dp))
            .padding(horizontal=8.dp,vertical=8.dp),
        horizontalArrangement=Arrangement.SpaceEvenly,
    ){
        SportDataItem(odo?.let{"%.1f".format(Locale.US,it)}?:"—","ODO",Modifier.weight(1f))
        SportDataItem("%.1f".format(Locale.US,trip),"TRIP",Modifier.weight(1f))
        SportDataItem(elapsed,"TIME",Modifier.weight(1f))
    }
}

@Composable
private fun SportDataItem(value:String,label:String,modifier:Modifier){
    Column(modifier,horizontalAlignment=Alignment.CenterHorizontally){
        Text(value,color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Black,maxLines=1,textAlign=TextAlign.Center)
        Text(label,color=Color(0xFFBFC6CD),fontSize=10.sp,fontWeight=FontWeight.Bold)
    }
}
