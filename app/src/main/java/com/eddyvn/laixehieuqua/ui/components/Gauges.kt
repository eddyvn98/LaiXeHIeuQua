package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.math.*

@Composable
fun SportSpeedGauge(speedKmh:Double,ecoTargetKmh:Double?,accent:Color,secondary:Color,modifier:Modifier=Modifier){
    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.43f;val center=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r);val topLeft=Offset(center.x-r,center.y-r)
            drawCircle(Color(0xFF111923),r,center,style=Stroke(15.dp.toPx()))
            drawArc(Color(0xFF26394A),140f,260f,false,topLeft,box,style=Stroke(2.dp.toPx()))
            val progress=(speedKmh/120.0).coerceIn(0.0,1.0).toFloat()
            drawArc(accent,140f,260f*progress,false,topLeft,box,style=Stroke(7.dp.toPx(),cap=StrokeCap.Round))
            ecoTargetKmh?.let{
                val angle=Math.toRadians(140.0+260.0*(it/120.0).coerceIn(0.0,1.0))
                val p1=Offset(center.x+(r-20.dp.toPx())*cos(angle).toFloat(),center.y+(r-20.dp.toPx())*sin(angle).toFloat())
                val p2=Offset(center.x+(r+4.dp.toPx())*cos(angle).toFloat(),center.y+(r+4.dp.toPx())*sin(angle).toFloat())
                drawLine(secondary,p1,p2,5.dp.toPx(),StrokeCap.Round)
            }
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            SegmentedNumber(speedKmh.roundToInt().toString(),onColor=accent)
            Text("KM/H",fontSize=10.sp,letterSpacing=2.sp,color=Color(0xFF8799AA))
            ecoTargetKmh?.let{Text("ECO "+it.roundToInt(),fontSize=13.sp,fontWeight=FontWeight.Bold,color=secondary,modifier=Modifier.padding(top=10.dp))}
        }
    }
}
@Composable
fun NeedleGauge(value:Double,range:ClosedFloatingPointRange<Double>,unit:String,accent:Color,modifier:Modifier=Modifier){
    Box(modifier,contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()){
            val r=size.minDimension*.40f;val c=Offset(size.width/2,size.height/2)
            val box=androidx.compose.ui.geometry.Size(2*r,2*r);val top=Offset(c.x-r,c.y-r)
            drawArc(Color(0xFF17212C),150f,240f,false,top,box,style=Stroke(7.dp.toPx()))
            drawArc(accent,150f,170f,false,top,box,style=Stroke(4.dp.toPx(),cap=StrokeCap.Round))
            val p=((value-range.start)/(range.endInclusive-range.start)).coerceIn(0.0,1.0)
            val a=Math.toRadians(150.0+240.0*p)
            drawLine(accent,c,Offset(c.x+r*.78f*cos(a).toFloat(),c.y+r*.78f*sin(a).toFloat()),3.dp.toPx(),StrokeCap.Round)
        }
        Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.offset(y=24.dp).background(Color(0xFF050A0F),RoundedCornerShape(9.dp)).padding(horizontal=10.dp,vertical=5.dp)){
            Text("%.1f".format(value),fontSize=16.sp,fontWeight=FontWeight.Bold)
            Text(unit,fontSize=8.sp,color=Color(0xFF7C8E9E))
        }
    }
}
