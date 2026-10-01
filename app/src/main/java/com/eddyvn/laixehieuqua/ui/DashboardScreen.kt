package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.domain.DriveSnapshot
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import com.eddyvn.laixehieuqua.ui.components.*
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    drive:DriveSnapshot,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    tripMeterKm:Double,
    onResetTrip:()->Unit,
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
                onResetTrip=onResetTrip,
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
                onResetTrip=onResetTrip,
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
    onResetTrip:()->Unit,
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
        DashboardHeader(statusLabel,trackingStatus,template,drive.rawGpsSpeedKmh,secondary)

        SportSpeedGauge(
            speedKmh=drive.displaySpeedKmh,
            ecoTargetKmh=drive.ecoTargetKmh,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.size(300.dp),
            actionLabel=gaugeActionLabel,
            clickEnabled=gaugeActionEnabled,
            onClick=onGaugeClick,
        )

        SubGaugeRow(drive,accent,secondary)

        InstrumentRow(
            vehicleOdometerKm=vehicleOdometerKm,
            tripMeterKm=tripMeterKm,
            averageSpeedKmh=drive.averageSpeedKmh,
            onResetTrip=onResetTrip,
        )

        SpeedTrendChart(
            speeds=drive.speedHistoryKmh,
            targetKmh=drive.ecoTargetKmh,
            modifier=Modifier.fillMaxWidth().padding(top=8.dp),
            chartHeight=62.dp,
        )
        EconomyGapChart(
            efficiency=drive.efficiencyHistory,
            modifier=Modifier.fillMaxWidth().padding(top=7.dp),
            chartHeight=76.dp,
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
    onResetTrip:()->Unit,
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
        DashboardHeader(statusLabel,trackingStatus,template,drive.rawGpsSpeedKmh,secondary)

        Row(
            Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement=Arrangement.spacedBy(16.dp),
            verticalAlignment=Alignment.CenterVertically,
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

            Column(
                Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement=Arrangement.SpaceEvenly,
            ){
                SubGaugeRow(drive,accent,secondary)

                InstrumentRow(
                    vehicleOdometerKm=vehicleOdometerKm,
                    tripMeterKm=tripMeterKm,
                    averageSpeedKmh=drive.averageSpeedKmh,
                    onResetTrip=onResetTrip,
                )

                SpeedTrendChart(
                    speeds=drive.speedHistoryKmh,
                    targetKmh=drive.ecoTargetKmh,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=42.dp,
                )

                EconomyGapChart(
                    efficiency=drive.efficiencyHistory,
                    modifier=Modifier.fillMaxWidth(),
                    chartHeight=48.dp,
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
){
    Row(
        Modifier.fillMaxWidth().padding(end=52.dp),
        horizontalArrangement=Arrangement.SpaceBetween,
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
        AdaptivePositiveGauge(
            value=drive.trueSpeedKmh,
            label="GPS",
            unit="km/h",
            accent=accent,
            secondary=secondary,
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
    onResetTrip:()->Unit,
){
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.SpaceEvenly,
        verticalAlignment=Alignment.CenterVertically,
    ){
        InstrumentMetric(vehicleOdometerKm,"ODO","km")
        InstrumentMetric(
            value=tripMeterKm,
            label="TRIP",
            unit="km",
            hint="GIỮ ĐỂ RESET",
            onLongPress=onResetTrip,
        )
        InstrumentMetric(averageSpeedKmh,"AVG","km/h")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstrumentMetric(
    value:Double?,
    label:String,
    unit:String,
    hint:String?=null,
    onLongPress:(()->Unit)?=null,
){
    val modifier=if(onLongPress!=null){
        Modifier.combinedClickable(
            onClick={},
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
