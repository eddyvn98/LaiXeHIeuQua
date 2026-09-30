package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.*

@Composable
fun SportSpeedGauge(
    speedKmh:Double,
    ecoTargetKmh:Double?,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
    actionLabel:String="CHẠM ĐỂ BẮT ĐẦU",
    clickEnabled:Boolean=true,
    onClick:()->Unit={},
){
    val animatedSpeed by animateFloatAsState(
        targetValue=speedKmh.toFloat().coerceAtLeast(0f),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="speed-gauge",
    )
    val animatedEcoTarget by animateFloatAsState(
        targetValue=(ecoTargetKmh?:0.0).toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="eco-target-gauge",
    )
    Box(
        modifier
            .clickable(enabled=clickEnabled,role=Role.Button,onClick=onClick)
            .semantics(mergeDescendants=true){contentDescription="Đồng hồ tốc độ. $actionLabel"},
        contentAlignment=Alignment.Center,
    ){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.43f
            val center=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r)
            val topLeft=Offset(center.x-r,center.y-r)
            drawCircle(Color(0xFF111923),r,center,style=Stroke(15.dp.toPx()))
            drawArc(Color(0xFF26394A),140f,260f,false,topLeft,box,style=Stroke(2.dp.toPx()))
            val progress=(animatedSpeed/120f).coerceIn(0f,1f)
            drawArc(accent,140f,260f*progress,false,topLeft,box,style=Stroke(7.dp.toPx(),cap=StrokeCap.Round))
            ecoTargetKmh?.let{
                val angle=Math.toRadians(140.0+260.0*(animatedEcoTarget/120f).coerceIn(0f,1f))
                val p1=Offset(center.x+(r-20.dp.toPx())*cos(angle).toFloat(),center.y+(r-20.dp.toPx())*sin(angle).toFloat())
                val p2=Offset(center.x+(r+4.dp.toPx())*cos(angle).toFloat(),center.y+(r+4.dp.toPx())*sin(angle).toFloat())
                drawLine(secondary,p1,p2,5.dp.toPx(),StrokeCap.Round)
            }
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            SegmentedNumber(String.format(java.util.Locale.US,"%.1f",animatedSpeed),onColor=accent)
            Text("KM/H",fontSize=10.sp,letterSpacing=2.sp,color=Color(0xFF8799AA))
            Text(actionLabel,fontSize=7.sp,letterSpacing=.8.sp,color=Color(0xFF718495),modifier=Modifier.padding(top=5.dp))
            ecoTargetKmh?.let{
                Text("ECO "+animatedEcoTarget.roundToInt(),fontSize=13.sp,fontWeight=FontWeight.Bold,color=secondary,modifier=Modifier.padding(top=10.dp))
            }
        }
    }
}

@Composable
fun AdaptivePositiveGauge(
    value:Double,
    label:String,
    unit:String,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
    initialMax:Float=80f,
    scaleBuckets:List<Float> = listOf(40f,60f,80f,120f,160f),
){
    val scale=rememberExpandingScale(value.toFloat().coerceAtLeast(0f),initialMax,scaleBuckets)
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat().coerceAtLeast(0f),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="positive-gauge-$label",
    )
    val progress=(animatedValue/scale).coerceIn(0f,1f)
    val liveColor=gaugeColor(progress,accent,secondary)
    val start=135f
    val sweep=270f
    val needleAngle=start+sweep*progress

    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.40f
            val c=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r)
            val top=Offset(c.x-r,c.y-r)
            drawArc(Color(0xFF17212C),start,sweep,false,top,box,style=Stroke(7.dp.toPx(),cap=StrokeCap.Round))
            drawArc(liveColor,start,sweep*progress,false,top,box,style=Stroke(5.dp.toPx(),cap=StrokeCap.Round))
            drawNeedle(c,r,needleAngle,liveColor,3.5.dp.toPx())
        }
        GaugeText(
            value="%.0f".format(animatedValue),
            label="$label $unit",
            scale="0–${scale.roundToInt()}",
            color=liveColor,
        )
    }
}

@Composable
fun AdaptiveAccelerationGauge(
    value:Double,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
){
    val scale=rememberExpandingScale(abs(value).toFloat(),2f,listOf(2f,4f,6f,8f,12f))
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="acceleration-gauge",
    )
    val normalized=(animatedValue/scale).coerceIn(-1f,1f)
    val severity=abs(normalized)
    val liveColor=gaugeColor(severity,accent,secondary)
    val zeroAngle=270f
    val needleAngle=zeroAngle+135f*normalized

    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.40f
            val c=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r)
            val top=Offset(c.x-r,c.y-r)
            drawArc(Color(0xFF17212C),135f,270f,false,top,box,style=Stroke(7.dp.toPx(),cap=StrokeCap.Round))
            drawSignedArc(top,box,needleAngle,zeroAngle,liveColor,5.dp.toPx())
            drawNeedle(c,r,needleAngle,liveColor,3.5.dp.toPx())
            drawNeedle(c,r,zeroAngle,Color(0xFF546474),1.2.dp.toPx(),length=.60f)
        }
        GaugeText(
            value="%+.1f".format(animatedValue),
            label="ACCEL m/s²",
            scale="±${scale.roundToInt()}",
            color=liveColor,
        )
    }
}

@Composable
fun SlopeGauge(
    value:Double,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
){
    val scale=rememberExpandingScale(abs(value).toFloat(),15f,listOf(15f,20f,30f,45f))
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="slope-gauge",
    )
    val normalized=(animatedValue/scale).coerceIn(-1f,1f)
    val severity=abs(normalized)
    val liveColor=gaugeColor(severity,accent,secondary)
    val direction=when{
        animatedValue > .7f -> "UP"
        animatedValue < -.7f -> "DOWN"
        else -> "LEVEL"
    }

    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val pivot=Offset(size.width*.18f,size.height*.47f)
            val armLength=size.width*.64f
            val referenceEnd=Offset(pivot.x+armLength,pivot.y)

            drawLine(
                Color(0xFF33424F),
                pivot,
                referenceEnd,
                2.dp.toPx(),
                StrokeCap.Round,
            )

            val displayAngle=-animatedValue.coerceIn(-scale,scale)
            val arcRadius=size.minDimension*.24f
            val arcTop=Offset(pivot.x-arcRadius,pivot.y-arcRadius)
            val arcBox=androidx.compose.ui.geometry.Size(arcRadius*2,arcRadius*2)
            if(abs(displayAngle)>.2f){
                drawArc(
                    liveColor,
                    if(displayAngle<0f)displayAngle else 0f,
                    abs(displayAngle),
                    false,
                    arcTop,
                    arcBox,
                    style=Stroke(4.dp.toPx(),cap=StrokeCap.Round),
                )
            }

            val angleRad=Math.toRadians(displayAngle.toDouble())
            val movingEnd=Offset(
                pivot.x+armLength*cos(angleRad).toFloat(),
                pivot.y+armLength*sin(angleRad).toFloat(),
            )
            drawLine(liveColor,pivot,movingEnd,5.dp.toPx(),StrokeCap.Round)
            drawCircle(Color(0xFF0A0F14),8.dp.toPx(),pivot)
            drawCircle(liveColor,5.dp.toPx(),pivot)

            val normalAngle=angleRad-Math.PI/2
            val nose=8.dp.toPx()
            drawLine(
                liveColor,
                movingEnd,
                Offset(
                    movingEnd.x+nose*cos(normalAngle).toFloat(),
                    movingEnd.y+nose*sin(normalAngle).toFloat(),
                ),
                3.dp.toPx(),
                StrokeCap.Round,
            )
        }

        Column(
            horizontalAlignment=Alignment.CenterHorizontally,
            modifier=Modifier.offset(y=30.dp),
        ){
            Text(
                if(direction=="LEVEL")"LEVEL 0°" else "$direction ${abs(animatedValue).roundToInt()}°",
                fontSize=14.sp,
                fontWeight=FontWeight.Bold,
                color=liveColor,
            )
            Text(
                "SLOPE ±${scale.roundToInt()}°",
                fontSize=7.sp,
                fontWeight=FontWeight.Bold,
                color=liveColor.copy(alpha=.88f),
            )
        }
    }
}

@Composable
private fun GaugeText(value:String,label:String,scale:String,color:Color){
    Column(
        horizontalAlignment=Alignment.CenterHorizontally,
        modifier=Modifier.offset(y=22.dp).padding(horizontal=4.dp,vertical=3.dp),
    ){
        Text(value,fontSize=15.sp,fontWeight=FontWeight.Bold,color=color)
        Text(label,fontSize=7.sp,fontWeight=FontWeight.Bold,color=color.copy(alpha=.88f))
        Text(scale,fontSize=7.sp,color=Color(0xFF7C8E9E))
    }
}

@Composable
private fun rememberExpandingScale(value:Float,initial:Float,buckets:List<Float>):Float{
    var scale by remember{mutableFloatStateOf(initial)}
    LaunchedEffect(value){
        val needed=value*1.15f
        if(needed>scale){
            scale=buckets.firstOrNull{it>=needed}
                ?: (ceil(needed/5f)*5f).coerceAtLeast(scale)
        }
    }
    return scale
}

private fun gaugeColor(progress:Float,accent:Color,secondary:Color):Color{
    val p=progress.coerceIn(0f,1f)
    return when{
        p<.50f -> lerp(accent,secondary,p/.50f)
        p<.78f -> lerp(secondary,Color(0xFFFFD166),(p-.50f)/.28f)
        else -> lerp(Color(0xFFFFD166),Color(0xFFFF5D73),(p-.78f)/.22f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSignedArc(
    top:Offset,
    box:androidx.compose.ui.geometry.Size,
    needleAngle:Float,
    zeroAngle:Float,
    color:Color,
    width:Float,
){
    if(needleAngle>=zeroAngle){
        drawArc(color,zeroAngle,needleAngle-zeroAngle,false,top,box,style=Stroke(width,cap=StrokeCap.Round))
    }else{
        drawArc(color,needleAngle,zeroAngle-needleAngle,false,top,box,style=Stroke(width,cap=StrokeCap.Round))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeedle(
    center:Offset,
    radius:Float,
    angleDeg:Float,
    color:Color,
    width:Float,
    length:Float=.78f,
){
    val a=Math.toRadians(angleDeg.toDouble())
    val end=Offset(
        center.x+radius*length*cos(a).toFloat(),
        center.y+radius*length*sin(a).toFloat(),
    )
    drawLine(color,center,end,width,StrokeCap.Round)
    drawCircle(color,radius*.045f,center)
}
