package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun SpeedTrendChart(
    speeds:List<Double>,
    targetKmh:Double?,
    modifier:Modifier=Modifier,
    chartHeight:Dp=54.dp,
){
    val values=speeds.takeLast(40).ifEmpty{listOf(0.0,0.0)}
    val maxValue=max(
        40.0,
        ceil(max(values.maxOrNull()?:0.0,targetKmh?:0.0)/20.0)*20.0,
    )

    Column(modifier){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text("SPEED",fontSize=9.sp,color=Color(0xFFD7E5EF),letterSpacing=1.2.sp)
            Text("0–${maxValue.toInt()} km/h",fontSize=8.sp,color=Color.White)
        }
        Canvas(Modifier.fillMaxWidth().height(chartHeight)){
            fun y(v:Double)=size.height*(1f-(v.coerceIn(0.0,maxValue)/maxValue).toFloat())

            targetKmh?.let{target->
                val ty=y(target)
                drawLine(
                    Color(0xFF64FFB5).copy(alpha=.55f),
                    Offset(0f,ty),
                    Offset(size.width,ty),
                    1.5.dp.toPx(),
                )
            }

            if(values.size>=2){
                val path=Path()
                values.forEachIndexed{index,value->
                    val x=size.width*index/values.lastIndex.toFloat()
                    val point=Offset(x,y(value))
                    if(index==0)path.moveTo(point.x,point.y) else path.lineTo(point.x,point.y)
                }
                drawPath(
                    path=path,
                    color=Color(0xFFFFFFFF),
                    style=Stroke(width=2.4.dp.toPx()),
                )
            }
        }
    }
}
