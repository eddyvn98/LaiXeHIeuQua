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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

@Composable
fun SpeedTrendChart(
    speeds:List<Double>,
    targetKmh:Double?,
    modifier:Modifier=Modifier,
    chartHeight:Dp=54.dp,
){
    val values=speeds.takeLast(40).ifEmpty{listOf(0.0,0.0)}
    val latest=values.lastOrNull()?:0.0
    val rawMin=values.minOrNull()?:0.0
    val rawMax=values.maxOrNull()?:0.0
    val spread=(rawMax-rawMin).coerceAtLeast(0.0)
    val padding=max(1.5,spread*.25)
    var minValue=floor((rawMin-padding)/2.0)*2.0
    var maxValue=ceil((rawMax+padding)/2.0)*2.0

    if(maxValue-minValue<6.0){
        val center=(maxValue+minValue)/2.0
        minValue=center-3.0
        maxValue=center+3.0
    }
    minValue=minValue.coerceAtLeast(0.0)
    if(maxValue<=minValue)maxValue=minValue+6.0
    val midValue=(minValue+maxValue)/2.0

    Column(modifier){
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.SpaceBetween,
        ){
            Text(
                "SPEED",
                fontSize=9.sp,
                color=Color(0xFFD7E5EF),
                letterSpacing=1.2.sp,
            )
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Text(
                    "${minValue.toInt()}–${maxValue.toInt()} km/h",
                    fontSize=8.sp,
                    color=Color(0xFF9FB4C2),
                )
                Text(
                    "NOW ${latest.toInt()} km/h",
                    fontSize=9.sp,
                    fontWeight=FontWeight.Bold,
                    color=Color.White,
                )
            }
        }

        Row(Modifier.fillMaxWidth()){
            Column(
                Modifier.width(24.dp).height(chartHeight),
                verticalArrangement=Arrangement.SpaceBetween,
            ){
                Text(maxValue.toInt().toString(),fontSize=7.sp,color=Color(0xFF90A4B3))
                Text(midValue.toInt().toString(),fontSize=7.sp,color=Color(0xFF90A4B3))
                Text(minValue.toInt().toString(),fontSize=7.sp,color=Color(0xFF90A4B3))
            }
            Canvas(Modifier.weight(1f).height(chartHeight)){
                fun y(v:Double)=size.height*(
                    1f-((v-minValue)/(maxValue-minValue)).toFloat().coerceIn(0f,1f)
                )

                drawLine(
                    Color(0xFF6B7C88).copy(alpha=.22f),
                    Offset(0f,size.height/2f),
                    Offset(size.width,size.height/2f),
                    1.dp.toPx(),
                )

                targetKmh?.takeIf{it in minValue..maxValue}?.let{target->
                    val ty=y(target)
                    drawLine(
                        Color(0xFF64FFB5).copy(alpha=.45f),
                        Offset(0f,ty),
                        Offset(size.width,ty),
                        1.dp.toPx(),
                    )
                }

                if(values.size>=2){
                    val points=values.mapIndexed{index,value->
                        Offset(
                            x=size.width*index/values.lastIndex.toFloat(),
                            y=y(value),
                        )
                    }
                    drawPath(
                        path=smoothChartPath(points),
                        color=Color(0xFF7EF4EF),
                        style=Stroke(
                            width=2.5.dp.toPx(),
                            cap=androidx.compose.ui.graphics.StrokeCap.Round,
                            join=androidx.compose.ui.graphics.StrokeJoin.Round,
                        ),
                    )
                    val last=points.last()
                    drawCircle(
                        color=Color(0xFFBFFFFD),
                        radius=3.5.dp.toPx(),
                        center=last,
                    )
                }
            }
        }
    }
}

private fun smoothChartPath(points:List<Offset>):Path=Path().apply{
    if(points.isEmpty())return@apply
    moveTo(points.first().x,points.first().y)
    for(index in 0 until points.lastIndex){
        val p0=points[(index-1).coerceAtLeast(0)]
        val p1=points[index]
        val p2=points[index+1]
        val p3=points[(index+2).coerceAtMost(points.lastIndex)]
        cubicTo(
            p1.x+(p2.x-p0.x)/6f,
            p1.y+(p2.y-p0.y)/6f,
            p2.x-(p3.x-p1.x)/6f,
            p2.y-(p3.y-p1.y)/6f,
            p2.x,
            p2.y,
        )
    }
}
