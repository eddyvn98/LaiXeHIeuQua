package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EconomyGapChart(
    efficiency:List<Double>,
    modifier:Modifier=Modifier,
    chartHeight:Dp=112.dp,
){
    val previousHistory=remember{mutableStateOf(efficiency)}
    val currentHistory=remember{mutableStateOf(efficiency)}
    val scrollProgress=remember{Animatable(1f)}

    LaunchedEffect(efficiency){
        if(efficiency!=currentHistory.value){
            previousHistory.value=currentHistory.value
            currentHistory.value=efficiency
            scrollProgress.snapTo(0f)
            scrollProgress.animateTo(1f,tween(550,easing=LinearEasing))
        }
    }

    val values=currentHistory.value.ifEmpty{listOf(100.0,100.0)}
    val previous=previousHistory.value.ifEmpty{values}
    val latest=values.lastOrNull()?:100.0

    Column(modifier.clipToBounds()){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text(
                "DRIVE EFFICIENCY",
                fontSize=10.sp,
                color=Color(0xFFD7E5EF),
                letterSpacing=1.4.sp,
            )
            Text(
                "${latest.toInt()} / 100 · MOVEMENT",
                fontSize=9.sp,
                color=Color(0xFFFFFFFF),
            )
        }
        Canvas(Modifier.fillMaxWidth().height(chartHeight).clipToBounds()){
            fun y(value:Double)=size.height*(1f-(value.coerceIn(0.0,100.0)/100.0).toFloat())

            drawLine(
                Color(0xFF64FFB5).copy(alpha=.35f),
                Offset(0f,y(80.0)),
                Offset(size.width,y(80.0)),
                1.dp.toPx(),
            )
            drawLine(
                Color(0xFFFFD166).copy(alpha=.28f),
                Offset(0f,y(60.0)),
                Offset(size.width,y(60.0)),
                1.dp.toPx(),
            )

            val historyWidth=size.width*.97f
            val points=historyPoints(previous,values,scrollProgress.value){v->y(v)}
                .map{it.copy(x=it.x*historyWidth)}

            if(points.size>=2){
                drawPath(
                    path=smoothPath(points),
                    color=Color(0xFF64FFB5),
                    style=Stroke(width=3.dp.toPx()),
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
