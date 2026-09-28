package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

private val segmentMap=mapOf(
    '0' to setOf(0,1,2,3,4,5),'1' to setOf(1,2),'2' to setOf(0,1,6,7,4,3),
    '3' to setOf(0,1,2,3,6,7),'4' to setOf(5,6,7,1,2),'5' to setOf(0,5,6,7,2,3),
    '6' to setOf(0,5,4,3,2,6,7),'7' to setOf(0,1,2),'8' to (0..7).toSet(),
    '9' to setOf(0,1,2,3,5,6,7),'-' to setOf(6,7)
)

@Composable
fun SegmentedNumber(text:String,modifier:Modifier=Modifier,onColor:Color=Color(0xFFE8FBFF),offColor:Color=onColor.copy(alpha=.07f)){
    Row(modifier,horizontalArrangement=Arrangement.spacedBy(5.dp)){
        text.forEach{ch->if(ch=='.')Dot(onColor)else Digit(ch,onColor,offColor)}
    }
}
@Composable private fun Dot(color:Color)=Canvas(Modifier.size(10.dp,62.dp)){
    drawCircle(color,radius=4.dp.toPx(),center=Offset(size.width/2,size.height-7.dp.toPx()))
}
@Composable private fun Digit(ch:Char,on:Color,off:Color)=Canvas(Modifier.size(36.dp,66.dp)){
    val active=segmentMap[ch].orEmpty();val t=5.dp.toPx();val mid=size.height/2
    fun h(y:Float,x:Float,w:Float,index:Int){
        val p=Path().apply{moveTo(x+t,y);lineTo(x+w-t,y);lineTo(x+w,y+t/2);lineTo(x+w-t,y+t);lineTo(x+t,y+t);lineTo(x,y+t/2);close()}
        drawPath(p,if(index in active)on else off)
    }
    fun v(x:Float,y:Float,height:Float,index:Int){
        val p=Path().apply{moveTo(x,y+t);lineTo(x+t/2,y);lineTo(x+t,y+t);lineTo(x+t,y+height-t);lineTo(x+t/2,y+height);lineTo(x,y+height-t);close()}
        drawPath(p,if(index in active)on else off)
    }
    h(t,t,size.width-2*t,0);v(size.width-t,2*t,mid-2.5f*t,1);v(size.width-t,mid+t/2,mid-2.5f*t,2)
    h(size.height-2*t,t,size.width-2*t,3);v(0f,mid+t/2,mid-2.5f*t,4);v(0f,2*t,mid-2.5f*t,5)
    h(mid-t/2,t,(size.width-2*t)/2,6);h(mid-t/2,size.width/2,(size.width-2*t)/2,7)
}
