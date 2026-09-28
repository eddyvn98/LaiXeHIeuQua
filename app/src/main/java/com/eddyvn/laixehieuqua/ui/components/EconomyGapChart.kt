package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import kotlin.math.*

@Composable
fun EconomyGapChart(speeds:List<Double>,targetKmh:Double?,modifier:Modifier=Modifier){
    val values=if(speeds.size>=2)speeds else listOf(0.0,0.0)
    val target=targetKmh?:values.lastOrNull()?:0.0
    val targetSeries=List(values.size){target}
    val projected=values.toMutableList().apply{
        val trend=if(size>=2)last()-this[size-2] else 0.0
        add((lastOrNull()?:target)+trend);add((lastOrNull()?:target)+trend)
    }
    val model=remember(values,target){
        CartesianChartModel(LineCartesianLayerModel.build{
            series(y=targetSeries);series(y=values);series(y=projected)
        })
    }
    val scale=max(3,ceil(values.maxOfOrNull{abs(it-target)}?:0.0).toInt()+1)
    Column(modifier){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
            Text("ECO GAP",fontSize=10.sp,color=Color(0xFF7E91A3),letterSpacing=1.5.sp)
            Text("AUTO ±"+scale+" km/h",fontSize=9.sp,color=Color(0xFFA3B4C3))
        }
        CartesianChartHost(chart=rememberCartesianChart(rememberLineCartesianLayer()),model=model,modifier=Modifier.fillMaxWidth().height(112.dp))
    }
}
