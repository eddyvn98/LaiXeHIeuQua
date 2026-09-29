package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            val r=size.minDimension*.43f;val center=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r);val topLeft=Offset(center.x-r,center.y-r)
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
            ecoTargetKmh?.let{Text("ECO "+animatedEcoTarget.roundToInt(),fontSize=13.sp,fontWeight=FontWeight.Bold,color=secondary,modifier=Modifier.padding(top=10.dp))}
        }
    }
}
@Composable
fun NeedleGauge(value:Double,range:ClosedFloatingPointRange<Double>,unit:String,accent:Color,modifier:Modifier=Modifier){
    val animatedValue by animateFloatAsState(
        targetValue=value.toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="needle-gauge-$unit",
    )
    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.40f;val c=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r);val top=Offset(c.x-r,c.y-r)
            drawArc(Color(0xFF17212C),150f,240f,false,top,box,style=Stroke(7.dp.toPx()))
            drawArc(accent,150f,170f,false,top,box,style=Stroke(4.dp.toPx(),cap=StrokeCap.Round))
            val p=((animatedValue-range.start.toFloat())/(range.endInclusive-range.start).toFloat()).coerceIn(0f,1f)
            val a=Math.toRadians(150.0+240.0*p)
            drawLine(accent,c,Offset(c.x+r*.78f*cos(a).toFloat(),c.y+r*.78f*sin(a).toFloat()),3.dp.toPx(),StrokeCap.Round)
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.offset(y=24.dp).padding(horizontal=6.dp,vertical=4.dp)){
            Text("%.1f".format(animatedValue),fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color.White)
            Text(unit,fontSize=8.sp,color=Color(0xFF7C8E9E))
        }
    }
}
