package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.GasStation
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.data.WeatherState
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import com.eddyvn.laixehieuqua.ui.components.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DashboardHeader(
    statusLabel:String,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    rawGpsSpeedKmh:Double,
    secondary:Color,
    onFuelClick:()->Unit,
){
    Row(
        Modifier.fillMaxWidth().padding(end=52.dp),
        horizontalArrangement=Arrangement.SpaceBetween,
        verticalAlignment=Alignment.CenterVertically,
    ){
        Text(
            statusLabel,
            color=if(trackingStatus==TrackingStatus.LIVE)secondary else Color.White,
            fontSize=11.sp,
            fontWeight=FontWeight.Bold,
        )
        Text(
            template?.name?:"Premium Segmented",
            color=Color(0xFFE0EAF1),
            fontSize=10.sp,
        )
        IconButton(
            onClick=onFuelClick,
            modifier=Modifier.size(34.dp),
        ){
            Icon(
                Tabler.Outline.GasStation,
                contentDescription="Đổ xăng",
                tint=secondary,
                modifier=Modifier.size(22.dp),
            )
        }
        Text(
            "GPS "+rawGpsSpeedKmh.roundToInt(),
            color=Color.White,
            fontSize=10.sp,
            fontWeight=FontWeight.Bold,
        )
    }
}

@Composable
internal fun SubGaugeRow(
    drive:DriveSnapshot,
    accent:Color,
    secondary:Color,
    modifier:Modifier=Modifier,
){
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.spacedBy(3.dp),
        verticalAlignment=Alignment.CenterVertically,
    ){
        RunningClockGauge(
            accent=accent,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
        AdaptiveAccelerationGauge(
            value=drive.accelerationMs2,
            averageMagnitudeMs2=drive.averageAccelerationMs2,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
        RunningSlopeGauge(
            value=drive.leanDeg,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
    }
}

@Composable
internal fun InstrumentRow(
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    sessionElapsedMs:Long,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
){
    Row(
        Modifier.fillMaxWidth().height(58.dp),
        horizontalArrangement=Arrangement.spacedBy(2.dp),
        verticalAlignment=Alignment.CenterVertically,
    ){
        InstrumentMetric(
            value=vehicleOdometerKm,
            label="ODO",
            unit="km",
            hint="CHẠM ĐỂ SỬA",
            modifier=Modifier.weight(1f),
            onClick=onOdoClick,
        )
        InstrumentMetric(
            value=tripMeterKm,
            label="TRIP",
            unit="km",
            hint="GIỮ ĐỂ RESET",
            modifier=Modifier.weight(1f),
            onLongPress=onResetTrip,
        )
        InstrumentTextMetric(
            value=formatDuration(sessionElapsedMs),
            label="THỜI GIAN",
            unit="",
            hint="BẮT ĐẦU → KẾT THÚC",
            modifier=Modifier.weight(1f),
            valueFontSize=14.sp,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstrumentMetric(
    value:Double?,
    label:String,
    unit:String,
    hint:String?=null,
    modifier:Modifier=Modifier,
    onClick:(()->Unit)?=null,
    onLongPress:(()->Unit)?=null,
){
    val interactionModifier=if(onClick!=null||onLongPress!=null){
        Modifier.combinedClickable(
            onClick=onClick?:{},
            onLongClick=onLongPress,
        )
    }else Modifier

    Column(
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Top,
        modifier=modifier.then(interactionModifier).fillMaxHeight().padding(start=2.dp,end=2.dp,top=4.dp),
    ){
        Text(
            value?.let{"%.1f".format(it)}?:"—",
            fontSize=16.sp,
            fontWeight=FontWeight.Bold,
            color=Color.White,
            maxLines=1,
            softWrap=false,
            overflow=TextOverflow.Ellipsis,
        )
        Text(
            "$label $unit",
            fontSize=8.sp,
            color=Color(0xFFD5E2EA),
            letterSpacing=.7.sp,
            fontWeight=FontWeight.Bold,
            maxLines=1,
            softWrap=false,
            overflow=TextOverflow.Clip,
        )
        hint?.let{
            Text(it,fontSize=6.sp,color=Color(0xFF9FB4C2),letterSpacing=.5.sp,maxLines=1,softWrap=false,overflow=TextOverflow.Clip)
        }
    }
}

@Composable
private fun InstrumentTextMetric(
    value:String,
    label:String,
    unit:String,
    hint:String?=null,
    modifier:Modifier=Modifier,
    valueFontSize:TextUnit=16.sp,
){
    Column(
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Top,
        modifier=modifier.fillMaxHeight().padding(start=2.dp,end=2.dp,top=4.dp),
    ){
        Text(
            value,
            fontSize=valueFontSize,
            fontWeight=FontWeight.Bold,
            color=Color.White,
            maxLines=1,
            softWrap=false,
            overflow=TextOverflow.Clip,
        )
        Text(
            listOf(label,unit).filter{it.isNotBlank()}.joinToString(" "),
            fontSize=8.sp,
            color=Color(0xFFD5E2EA),
            letterSpacing=.7.sp,
            fontWeight=FontWeight.Bold,
            maxLines=1,
            softWrap=false,
            overflow=TextOverflow.Clip,
        )
        hint?.let{
            Text(it,fontSize=6.sp,color=Color(0xFF9FB4C2),letterSpacing=.5.sp,maxLines=1,softWrap=false,overflow=TextOverflow.Clip)
        }
    }
}

@Composable
internal fun rememberDisplayedSessionElapsed(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
):Long{
    val active=trackingStatus in setOf(
        TrackingStatus.STARTING,
        TrackingStatus.WAITING_FOR_GPS,
        TrackingStatus.LIVE,
        TrackingStatus.STOPPING,
    )
    var nowMs by remember{mutableLongStateOf(System.currentTimeMillis())}
    LaunchedEffect(active,drive.sessionStartMs){
        while(active){
            nowMs=System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val liveElapsed=drive.sessionStartMs?.let{
        (nowMs-it).coerceAtLeast(0L)
    }?:0L
    return if(active){
        maxOf(drive.sessionElapsedMs,liveElapsed)
    }else{
        drive.sessionElapsedMs
    }
}

private fun formatDuration(ms:Long):String{
    val totalSeconds=(ms.coerceAtLeast(0L)/1000L)
    val hours=totalSeconds/3600L
    val minutes=(totalSeconds%3600L)/60L
    val seconds=totalSeconds%60L
    return "%02d:%02d:%02d".format(hours,minutes,seconds)
}

@Composable
internal fun DriveStatusHint(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    secondary:Color,
){
    when{
        trackingStatus==TrackingStatus.WAITING_FOR_GPS->
            Text("Đang chờ GPS · ra nơi thoáng",fontSize=11.sp,color=Color.White)
        trackingStatus==TrackingStatus.PERMISSION_REQUIRED->
            Text("Cần quyền vị trí",fontSize=11.sp,color=Color.White)
        trackingStatus==TrackingStatus.ERROR->
            Text("Không khởi động được GPS",fontSize=11.sp,color=Color(0xFFFFD166))
        drive.traffic->
            Text("STOP/GO · không phạt hiệu quả vì kẹt xe",fontSize=10.sp,color=Color(0xFFFFD166))
        drive.ecoTargetKmh!=null->
            Text(
                "ECO TARGET ${drive.ecoTargetKmh.roundToInt()} km/h",
                fontSize=10.sp,
                color=secondary,
                fontWeight=FontWeight.Bold,
            )
    }
}


@Composable
internal fun WeatherDateStrip(
    weather:WeatherState,
    onRefresh:()->Unit,
    compact:Boolean=false,
){
    var nowMs by remember{mutableLongStateOf(System.currentTimeMillis())}
    LaunchedEffect(Unit){
        while(true){
            nowMs=System.currentTimeMillis()
            delay(60_000L)
        }
    }
    val locale=remember{Locale.forLanguageTag("vi-VN")}
    val dateText=remember(nowMs){
        SimpleDateFormat("EEEE, dd/MM/yyyy",locale).format(Date(nowMs))
            .replaceFirstChar{if(it.isLowerCase())it.titlecase(locale) else it.toString()}
    }
    val weatherText=when{
        weather.loading->"Đang tải thời tiết…"
        weather.currentTempC!=null->buildString{
            append("%.0f°C · %s".format(weather.currentTempC,weatherLabel(weather.weatherCode)))
            if(weather.todayMaxC!=null&&weather.todayMinC!=null){
                append(" · %.0f/%.0f°C".format(weather.todayMaxC,weather.todayMinC))
            }
            weather.todayRainChance?.let{append(" · mưa $it%")}
        }
        weather.error!=null->weather.error
        else->"Chưa có dự báo"
    }
    val tomorrow=if(
        weather.tomorrowMaxC!=null&&weather.tomorrowMinC!=null
    ){
        "Mai %.0f/%.0f°C%s".format(
            weather.tomorrowMaxC,
            weather.tomorrowMinC,
            weather.tomorrowRainChance?.let{" · mưa $it%"}?:"",
        )
    }else null

    Row(
        Modifier.fillMaxWidth().padding(horizontal=if(compact)0.dp else 2.dp,vertical=if(compact)1.dp else 3.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(6.dp),
    ){
        Column(Modifier.weight(1f)){
            Text(
                dateText,
                fontSize=if(compact)9.sp else 10.sp,
                color=Color(0xFFD5E2EA),
                fontWeight=FontWeight.Bold,
                maxLines=1,
            )
            Text(
                weatherText,
                fontSize=if(compact)9.sp else 10.sp,
                color=Color.White,
                maxLines=1,
                overflow=TextOverflow.Ellipsis,
            )
            if(!compact&&tomorrow!=null){
                Text(tomorrow,fontSize=8.sp,color=Color(0xFF9FB4C2),maxLines=1)
            }
        }
        TextButton(
            onClick=onRefresh,
            enabled=!weather.loading,
            contentPadding=PaddingValues(horizontal=6.dp,vertical=0.dp),
            modifier=Modifier.height(28.dp),
        ){Text("↻",fontSize=15.sp)}
    }
}

private fun weatherLabel(code:Int?):String=when(code){
    0->"Trời quang"
    1,2->"Ít mây"
    3->"Nhiều mây"
    45,48->"Sương mù"
    51,53,55,56,57->"Mưa phùn"
    61,63,65,66,67->"Mưa"
    71,73,75,77->"Tuyết"
    80,81,82->"Mưa rào"
    85,86->"Mưa tuyết"
    95,96,99->"Dông"
    else->"Thời tiết"
}
