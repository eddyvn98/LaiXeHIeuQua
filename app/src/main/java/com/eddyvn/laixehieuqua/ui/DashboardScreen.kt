package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.GasStation
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.domain.FuelSummary
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import com.eddyvn.laixehieuqua.ui.components.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    fuelSummary:FuelSummary,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    onStart:()->Unit,
    onStop:()->Unit,
){
    val accent=Color(template?.accentHex?:0xFF7EEBFF)
    val secondary=Color(template?.secondaryHex?:0xFF7CFFC0)
    val trackingActive=trackingStatus in setOf(
        TrackingStatus.STARTING,
        TrackingStatus.WAITING_FOR_GPS,
        TrackingStatus.LIVE,
        TrackingStatus.STOPPING,
    )
    val statusLabel=when(trackingStatus){
        TrackingStatus.IDLE->"READY"
        TrackingStatus.REQUESTING_PERMISSION->"CHECKING PERMISSION"
        TrackingStatus.PERMISSION_REQUIRED->"LOCATION REQUIRED"
        TrackingStatus.STARTING->"STARTING RIDE"
        TrackingStatus.WAITING_FOR_GPS->"WAITING FOR GPS"
        TrackingStatus.LIVE->"● LIVE"
        TrackingStatus.STOPPING->"STOPPING"
        TrackingStatus.ERROR->"START FAILED"
    }
    val gaugeActionLabel=when(trackingStatus){
        TrackingStatus.REQUESTING_PERMISSION->"ĐANG XIN QUYỀN"
        TrackingStatus.STARTING->"ĐANG BẮT ĐẦU"
        TrackingStatus.STOPPING->"ĐANG DỪNG"
        else->if(trackingActive)"CHẠM ĐỂ DỪNG" else "CHẠM ĐỂ BẮT ĐẦU"
    }
    val gaugeAction=if(trackingActive)onStop else onStart
    val displayedSessionElapsedMs=rememberDisplayedSessionElapsed(drive,trackingStatus)
    val gaugeClickEnabled=trackingStatus !in setOf(
        TrackingStatus.REQUESTING_PERMISSION,
        TrackingStatus.STOPPING,
    )

    BoxWithConstraints(Modifier.fillMaxSize()){
        if(maxWidth>maxHeight){
            LandscapeDashboardContent(
                drive=drive,
                trackingStatus=trackingStatus,
                template=template,
                vehicleOdometerKm=vehicleOdometerKm,
                tripMeterKm=tripMeterKm,
                sessionElapsedMs=displayedSessionElapsedMs,
                fuelSummary=fuelSummary,
                onResetTrip=onResetTrip,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                statusLabel=statusLabel,
                gaugeActionLabel=gaugeActionLabel,
                gaugeActionEnabled=gaugeClickEnabled,
                accent=accent,
                secondary=secondary,
                onGaugeClick=gaugeAction,
                maxWidth=maxWidth,
                maxHeight=maxHeight,
            )
        }else{
            PortraitDashboardContent(
                drive=drive,
                trackingStatus=trackingStatus,
                template=template,
                vehicleOdometerKm=vehicleOdometerKm,
                tripMeterKm=tripMeterKm,
                sessionElapsedMs=displayedSessionElapsedMs,
                fuelSummary=fuelSummary,
                onResetTrip=onResetTrip,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                statusLabel=statusLabel,
                gaugeActionLabel=gaugeActionLabel,
                gaugeActionEnabled=gaugeClickEnabled,
                accent=accent,
                secondary=secondary,
                onGaugeClick=gaugeAction,
            )
        }
    }
}

@Composable
private fun PortraitDashboardContent(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    sessionElapsedMs:Long,
    fuelSummary:FuelSummary,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    statusLabel:String,
    gaugeActionLabel:String,
    gaugeActionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    onGaugeClick:()->Unit,
){
    Column(
        Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=10.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
    ){
        DashboardHeader(
            statusLabel,trackingStatus,template,drive.rawGpsSpeedKmh,secondary,onFuelClick
        )

        SportSpeedGauge(
            speedKmh=drive.displaySpeedKmh,
            ecoTargetKmh=drive.ecoTargetKmh,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.size(292.dp),
            actionLabel=gaugeActionLabel,
            clickEnabled=gaugeActionEnabled,
            onClick=onGaugeClick,
        )

        SpeedTrendChart(
            speeds=drive.speedHistoryKmh,
            targetKmh=drive.ecoTargetKmh,
            modifier=Modifier.fillMaxWidth(.90f).padding(top=4.dp),
            chartHeight=46.dp,
        )

        SubGaugeRow(drive,accent,secondary)

        InstrumentRow(
            vehicleOdometerKm=vehicleOdometerKm,
            tripMeterKm=tripMeterKm,
            averageSpeedKmh=drive.averageSpeedKmh,
            sessionElapsedMs=sessionElapsedMs,
            onResetTrip=onResetTrip,
            onOdoClick=onOdoClick,
        )

        FuelEstimateStrip(
            summary=fuelSummary,
            modifier=Modifier.fillMaxWidth().padding(top=6.dp),
        )

        EconomyGapChart(
            efficiency=drive.efficiencyHistory,
            modifier=Modifier.fillMaxWidth().padding(top=6.dp),
            chartHeight=62.dp,
        )

        DriveStatusHint(drive,trackingStatus,secondary)
    }
}

@Composable
private fun LandscapeDashboardContent(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    sessionElapsedMs:Long,
    fuelSummary:FuelSummary,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    statusLabel:String,
    gaugeActionLabel:String,
    gaugeActionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    onGaugeClick:()->Unit,
    maxWidth:Dp,
    maxHeight:Dp,
){
    val gaugeSize=minOf(270.dp,maxHeight*.84f,maxWidth*.38f)
    Column(Modifier.fillMaxSize().padding(horizontal=14.dp,vertical=6.dp)){
        DashboardHeader(
            statusLabel,trackingStatus,template,drive.rawGpsSpeedKmh,secondary,onFuelClick
        )

        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement=Arrangement.spacedBy(16.dp),
            verticalAlignment=Alignment.CenterVertically,
        ){
            Column(
                modifier=Modifier.width(gaugeSize+24.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.Center,
            ){
                SportSpeedGauge(
                    speedKmh=drive.displaySpeedKmh,
                    ecoTargetKmh=drive.ecoTargetKmh,
                    accent=accent,
                    secondary=secondary,
                    modifier=Modifier.size(gaugeSize),
                    actionLabel=gaugeActionLabel,
                    clickEnabled=gaugeActionEnabled,
                    onClick=onGaugeClick,
                )
                SpeedTrendChart(
                    speeds=drive.speedHistoryKmh,
                    targetKmh=drive.ecoTargetKmh,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=42.dp,
                )
            }

            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement=Arrangement.SpaceEvenly,
            ){
                SubGaugeRow(drive,accent,secondary)

                InstrumentRow(
                    vehicleOdometerKm=vehicleOdometerKm,
                    tripMeterKm=tripMeterKm,
                    averageSpeedKmh=drive.averageSpeedKmh,
                    sessionElapsedMs=sessionElapsedMs,
                    onResetTrip=onResetTrip,
                    onOdoClick=onOdoClick,
                )

                FuelEstimateStrip(
                    summary=fuelSummary,
                    modifier=Modifier.fillMaxWidth(),
                )

                EconomyGapChart(
                    efficiency=drive.efficiencyHistory,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=40.dp,
                )

                DriveStatusHint(drive,trackingStatus,secondary)
            }
        }
    }
}

@Composable
private fun DashboardHeader(
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
private fun SubGaugeRow(
    drive:DriveSnapshot,
    accent:Color,
    secondary:Color,
){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.spacedBy(3.dp),
        verticalAlignment=Alignment.CenterVertically,
    ){
        DigitalClockGauge(
            accent=accent,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
        AdaptiveAccelerationGauge(
            value=drive.accelerationMs2,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
        SlopeGauge(
            value=drive.leanDeg,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.weight(1f).aspectRatio(1f),
        )
    }
}

@Composable
private fun InstrumentRow(
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    averageSpeedKmh:Double,
    sessionElapsedMs:Long,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.SpaceEvenly,
        verticalAlignment=Alignment.CenterVertically,
    ){
        InstrumentMetric(
            value=vehicleOdometerKm,
            label="ODO",
            unit="km",
            hint="CHẠM ĐỂ SỬA",
            onClick=onOdoClick,
        )
        InstrumentMetric(
            value=tripMeterKm,
            label="TRIP",
            unit="km",
            hint="GIỮ ĐỂ RESET",
            onLongPress=onResetTrip,
        )
        InstrumentMetric(averageSpeedKmh,"AVG","km/h")
        InstrumentTextMetric(
            value=formatDuration(sessionElapsedMs),
            label="THỜI GIAN",
            unit="",
            hint="BẮT ĐẦU → KẾT THÚC",
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
    onClick:(()->Unit)?=null,
    onLongPress:(()->Unit)?=null,
){
    val modifier=if(onClick!=null||onLongPress!=null){
        Modifier.combinedClickable(
            onClick=onClick?:{},
            onLongClick=onLongPress,
        )
    }else Modifier

    Column(
        horizontalAlignment=Alignment.CenterHorizontally,
        modifier=modifier.padding(horizontal=8.dp,vertical=3.dp),
    ){
        Text(
            value?.let{"%.1f".format(it)}?:"—",
            fontSize=18.sp,
            fontWeight=FontWeight.Bold,
            color=Color.White,
        )
        Text(
            "$label $unit",
            fontSize=8.sp,
            color=Color(0xFFD5E2EA),
            letterSpacing=.7.sp,
            fontWeight=FontWeight.Bold,
        )
        hint?.let{
            Text(it,fontSize=6.sp,color=Color(0xFF9FB4C2),letterSpacing=.5.sp)
        }
    }
}

@Composable
private fun InstrumentTextMetric(
    value:String,
    label:String,
    unit:String,
    hint:String?=null,
){
    Column(
        horizontalAlignment=Alignment.CenterHorizontally,
        modifier=Modifier.padding(horizontal=8.dp,vertical=3.dp),
    ){
        Text(
            value,
            fontSize=16.sp,
            fontWeight=FontWeight.Bold,
            color=Color.White,
        )
        Text(
            listOf(label,unit).filter{it.isNotBlank()}.joinToString(" "),
            fontSize=8.sp,
            color=Color(0xFFD5E2EA),
            letterSpacing=.7.sp,
            fontWeight=FontWeight.Bold,
        )
        hint?.let{
            Text(it,fontSize=6.sp,color=Color(0xFF9FB4C2),letterSpacing=.5.sp)
        }
    }
}

@Composable
private fun rememberDisplayedSessionElapsed(
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
private fun DriveStatusHint(
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
