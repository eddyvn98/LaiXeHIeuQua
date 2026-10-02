package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

@Composable
fun RunningClockGauge(
    accent:Color,
    modifier:Modifier=Modifier,
){
    var nowMs by remember{mutableLongStateOf(System.currentTimeMillis())}
    LaunchedEffect(Unit){
        while(true){
            nowMs=System.currentTimeMillis()
            delay(250L)
        }
    }
    val time=remember(nowMs){
        SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date(nowMs))
    }
    val minuteProgress=(nowMs%60_000L)/60_000f
    val animatedProgress by animateFloatAsState(
        targetValue=minuteProgress,
        animationSpec=tween(230,easing=FastOutSlowInEasing),
        label="clock-minute-progress",
    )

    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize().padding(4.dp)){
            val radius=size.minDimension*.45f
            val center=Offset(size.width/2f,size.height/2f)
            val box=androidx.compose.ui.geometry.Size(radius*2,radius*2)
            val top=Offset(center.x-radius,center.y-radius)
            drawArc(
                Color(0xFF405364),135f,270f,false,top,box,
                style=Stroke(4.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round),
            )
            if(animatedProgress>0f){
                drawArc(
                    accent,135f,270f*animatedProgress,false,top,box,
                    style=Stroke(4.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
            val angle=Math.toRadians((135f+270f*animatedProgress).toDouble())
            drawCircle(
                accent,
                3.5.dp.toPx(),
                Offset(
                    center.x+radius*cos(angle).toFloat(),
                    center.y+radius*sin(angle).toFloat(),
                ),
            )
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text(time,color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,maxLines=1)
            Text("GIỜ HIỆN TẠI",color=Color(0xFFB9C9D4),fontSize=8.sp,maxLines=1)
        }
    }
}

@Composable
fun RunningAccelerationGauge(
    value:Double,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
){
    val scaleTarget=autoRange(abs(value).toFloat(),listOf(2f,4f,6f,8f,12f,16f,24f,32f,50f))
    var range by remember{mutableFloatStateOf(2f)}
    LaunchedEffect(scaleTarget){
        if(scaleTarget>range) range=scaleTarget
        else if(scaleTarget<range){
            delay(3_000L)
            range=scaleTarget
        }
    }
    val animatedRange by animateFloatAsState(
        targetValue=range,
        animationSpec=tween(600,easing=FastOutSlowInEasing),
        label="acceleration-auto-range",
    )
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat().coerceIn(-50f,50f),
        animationSpec=tween(120,easing=FastOutSlowInEasing),
        label="acceleration-reading",
    )
    val progress=(abs(animatedValue)/animatedRange).coerceIn(0f,1f)
    val liveColor=runningMetricColor(progress,accent,secondary)

    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val radius=size.minDimension*.45f
            val center=Offset(size.width/2f,size.height/2f)
            val box=androidx.compose.ui.geometry.Size(radius*2,radius*2)
            val top=Offset(center.x-radius,center.y-radius)
            drawArc(
                Color(0xFF405364),135f,270f,false,top,box,
                style=Stroke(6.dp.toPx(),cap=StrokeCap.Round),
            )
            if(progress>0f){
                drawArc(
                    liveColor,135f,270f*progress,false,top,box,
                    style=Stroke(5.dp.toPx(),cap=StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text(
                String.format(Locale.US,"%+.1f",animatedValue),
                color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Bold,maxLines=1,
            )
            Text("ACCEL m/s²",color=Color(0xFFD7E5EF),fontSize=9.sp,maxLines=1)
            Text("±${animatedRange.roundToInt()}",color=Color(0xFFB9C9D4),fontSize=8.sp,maxLines=1)
        }
    }
}

@Composable
fun RunningSlopeGauge(
    value:Double,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
){
    val scaleTarget=autoRange(abs(value).toFloat(),listOf(5f,8f,12f,18f,25f,35f,50f))
    var range by remember{mutableFloatStateOf(5f)}
    LaunchedEffect(scaleTarget){
        if(scaleTarget>range) range=scaleTarget
        else if(scaleTarget<range){
            delay(3_000L)
            range=scaleTarget
        }
    }
    val animatedRange by animateFloatAsState(
        targetValue=range,
        animationSpec=tween(600,easing=FastOutSlowInEasing),
        label="slope-auto-range",
    )
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat().coerceIn(-50f,50f),
        animationSpec=tween(550,easing=FastOutSlowInEasing),
        label="slope-road-angle",
    )
    val progress=(abs(animatedValue)/animatedRange).coerceIn(0f,1f)
    val liveColor=runningMetricColor(progress,accent,secondary)
    val angle=(animatedValue/animatedRange).coerceIn(-1f,1f)*30f
    val direction=when{
        animatedValue>.7f->"LÊN DỐC"
        animatedValue<-.7f->"XUỐNG DỐC"
        else->"ĐƯỜNG NGANG"
    }

    BoxWithConstraints(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val centerX=size.width/2f
            val centerY=size.height*.36f
            val halfSpan=size.width*.31f
            val halfRise=halfSpan*tan(Math.toRadians(angle.toDouble())).toFloat()
            val left=Offset(centerX-halfSpan,centerY+halfRise)
            val right=Offset(centerX+halfSpan,centerY-halfRise)
            drawLine(Color(0xFF35495A),Offset(left.x,centerY),Offset(right.x,centerY),2.dp.toPx(),StrokeCap.Round)
            drawLine(liveColor,left,right,5.dp.toPx(),StrokeCap.Round)
            drawCircle(liveColor,5.dp.toPx(),left)
            drawCircle(liveColor,5.dp.toPx(),right)
            drawCircle(Color(0xFF0A0F14),2.dp.toPx(),left)
            drawCircle(Color(0xFF0A0F14),2.dp.toPx(),right)
        }
        Column(
            Modifier.fillMaxSize().padding(top=maxHeight*.48f),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Top,
        ){
            Text(
                "$direction ${String.format(Locale.US,"%+.1f°",animatedValue)}",
                color=liveColor,fontSize=14.sp,fontWeight=FontWeight.Bold,maxLines=1,
            )
            Text(
                "THANG ±${animatedRange.roundToInt()}°",
                color=Color(0xFFB9C9D4),fontSize=8.sp,maxLines=1,
            )
        }
    }
}

fun runningMetricColor(progress:Float,accent:Color,secondary:Color):Color{
    val p=progress.coerceIn(0f,1f)
    return when{
        p<.5f->lerp(accent,secondary,p/.5f)
        p<.8f->lerp(secondary,Color(0xFFFFD166),(p-.5f)/.3f)
        else->lerp(Color(0xFFFFD166),Color(0xFFFF5D73),(p-.8f)/.2f)
    }
}

private fun autoRange(value:Float,buckets:List<Float>):Float{
    val needed=(value*1.25f).coerceAtLeast(buckets.first())
    return buckets.firstOrNull{it>=needed}?:ceil(needed/10f)*10f
}
