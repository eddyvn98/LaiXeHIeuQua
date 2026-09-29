package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun EconomyGapChart(
    speeds:List<Double>,
    targetKmh:Double?,
    modifier:Modifier=Modifier,
    chartHeight:Dp=112.dp,
){
    val previousHistory=remember{mutableStateOf(speeds)}
    val currentHistory=remember{mutableStateOf(speeds)}
    val scrollProgress=remember{Animatable(1f)}
    LaunchedEffect(speeds){
        if(speeds!=currentHistory.value){
            previousHistory.value=currentHistory.value
            currentHistory.value=speeds
            scrollProgress.snapTo(0f)
            scrollProgress.animateTo(1f,tween(900,easing=LinearEasing))
        }
    }

    val values=currentHistory.value.ifEmpty{listOf(0.0,0.0)}
    val previous=previousHistory.value.ifEmpty{values}
    val target=targetKmh?:values.lastOrNull()?:0.0
    val animatedTarget by animateFloatAsState(
        targetValue=target.toFloat(),
        animationSpec=tween(700,easing=LinearEasing),
        label="eco-chart-target",
    )
    val scale=max(3,ceil((values+previous).maxOfOrNull{abs(it-target)}?:0.0).toInt()+1)

    Column(modifier.clipToBounds()){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text("ECO GAP",fontSize=10.sp,color=Color(0xFF7E91A3),letterSpacing=1.5.sp)
            Text("AUTO ±$scale km/h",fontSize=9.sp,color=Color(0xFFA3B4C3))
        }
        Canvas(Modifier.fillMaxWidth().height(chartHeight).clipToBounds()){
            val range=scale.toFloat()
            fun y(value:Float)=size.height*(0.5f-(value-animatedTarget)/(range*2f))
            val targetY=y(animatedTarget)
            drawLine(Color(0xFF347FFF),Offset(0f,targetY),Offset(size.width,targetY),1.5.dp.toPx())

            val historyWidth=size.width*0.94f
            val points=historyPoints(previous,values,scrollProgress.value){value->y(value.toFloat())}
                .map{it.copy(x=it.x*historyWidth)}
            if(points.size>=2){
                drawPath(
                    path=smoothPath(points),
                    color=Color(0xFFFFB000),
                    style=Stroke(width=2.5.dp.toPx()),
                )
            }
            if(points.isNotEmpty()&&values.size>=2){
                val trend=(values.last()-values[values.lastIndex-1]).toFloat()
                val last=points.last()
                val projected=Path().apply{
                    moveTo(last.x,last.y)
                    lineTo(size.width*0.97f,y(values.last().toFloat()+trend))
                    lineTo(size.width,y(values.last().toFloat()+trend*2f))
                }
                drawPath(
                    path=projected,
                    color=Color(0xFFFFB000).copy(alpha=0.55f),
                    style=Stroke(
                        width=2.dp.toPx(),
                        pathEffect=PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(),4.dp.toPx())),
                    ),
                )
            }
        }
    }
}

private fun historyPoints(
    previous:List<Double>,
    current:List<Double>,
    progress:Float,
    mapY:(Double)->Float,
):List<Offset>{
    if(previous.size<2||current.size<2||current.size<previous.size){
        return current.mapIndexed{index,value->
            Offset(index.toFloat()/(current.lastIndex.coerceAtLeast(1)),mapY(value))
        }
    }

    if(current.size>previous.size){
        val pointCount=current.size
        val older=previous.takeLast(pointCount-1)
        val fixed=older.mapIndexed{index,value->
            Offset(index.toFloat()/(pointCount-1),mapY(value))
        }
        val oldTail=previous.last()
        val newTail=current.last()
        return fixed+Offset(1f,mapY(oldTail+(newTail-oldTail)*progress))
    }

    val denominator=previous.lastIndex.coerceAtLeast(1).toFloat()
    val shifted=previous.mapIndexed{index,value->
        Offset((index-progress)/denominator,mapY(value))
    }
    val oldTail=previous.last()
    val newTail=current.last()
    val entering=Offset(
        (previous.size-progress)/denominator,
        mapY(oldTail+(newTail-oldTail)*progress),
    )
    return shifted+entering
}

private fun smoothPath(points:List<Offset>):Path=Path().apply{
    moveTo(points.first().x,points.first().y)
    for(index in 0 until points.lastIndex){
        val p0=points[(index-1).coerceAtLeast(0)]
        val p1=points[index]
        val p2=points[index+1]
        val p3=points[(index+2).coerceAtMost(points.lastIndex)]
        cubicTo(
            p1.x+(p2.x-p0.x)/6f,p1.y+(p2.y-p0.y)/6f,
            p2.x-(p3.x-p1.x)/6f,p2.y-(p3.y-p1.y)/6f,
            p2.x,p2.y,
        )
    }
}
