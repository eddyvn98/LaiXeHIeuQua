package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.domain.*
import com.eddyvn.laixehieuqua.ui.components.*
import com.eddyvn.laixehieuqua.tracking.TrackingStatus
import kotlin.math.*

@Composable
fun DashboardScreen(
    drive:DriveSnapshot,
    reference:EconomyReference?,
    summary:FuelSummary,
    projection:EconomyProjection?,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    onStart:()->Unit,
    onStop:()->Unit,
){
    val accent=Color(template?.accentHex?:0xFF5CE7FF)
    val secondary=Color(template?.secondaryHex?:0xFF68FFB2)
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
                reference=reference,
                summary=summary,
                projection=projection,
                trackingStatus=trackingStatus,
                template=template,
                vehicleOdometerKm=vehicleOdometerKm,
                statusLabel=statusLabel,
                gaugeActionLabel=gaugeActionLabel,
                gaugeActionEnabled=gaugeClickEnabled,
                accent=accent,
                secondary=secondary,
                onGaugeClick=gaugeAction,
                maxWidth=maxWidth,
                maxHeight=maxHeight,
            )
        }else Column(
        Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=12.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
    ){
        Row(Modifier.fillMaxWidth().padding(end=52.dp),horizontalArrangement=Arrangement.SpaceBetween){
            Text(statusLabel,color=if(trackingStatus==TrackingStatus.LIVE)secondary else Color(0xFF708295),fontSize=11.sp)
            Text(if(drive.traffic)"TRAFFIC" else (template?.name?:"TFT Sport Bike"),color=Color(0xFF8496A7),fontSize=10.sp)
            Text("GPS "+drive.rawGpsSpeedKmh.roundToInt(),color=Color(0xFF8496A7),fontSize=10.sp)
        }

        SportSpeedGauge(
            speedKmh=drive.displaySpeedKmh,
            ecoTargetKmh=drive.ecoTargetKmh,
            accent=accent,
            secondary=secondary,
            modifier=Modifier.size(310.dp),
            actionLabel=gaugeActionLabel,
            clickEnabled=gaugeClickEnabled,
            onClick=gaugeAction,
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(4.dp),
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
        Row(Modifier.fillMaxWidth().padding(top=4.dp),horizontalArrangement=Arrangement.SpaceEvenly){
            InstrumentMetric(vehicleOdometerKm,"ODO")
            InstrumentMetric(drive.distanceKm,"TRIP")
        }

        if(drive.traffic){
            Text("STOP/GO",color=Color(0xFFFFD166),fontWeight=FontWeight.Bold,fontSize=12.sp)
        }else drive.ecoTargetKmh?.let{target->
            Text(
                "Δ ECO "+"%+.0f".format(drive.displaySpeedKmh-target)+" km/h",
                color=secondary,
                fontWeight=FontWeight.Bold,
                fontSize=12.sp,
            )
        }

        EconomyGapChart(drive.speedHistoryKmh,drive.ecoTargetKmh,Modifier.fillMaxWidth().padding(top=6.dp))

        Row(Modifier.fillMaxWidth().padding(top=4.dp),horizontalArrangement=Arrangement.SpaceEvenly){
            Metric(summary.currentCycleKm,"CURRENT")
            Metric(projection?.projectedCycleKm?:0.0,"PROJECTED")
            Metric(reference?.cycle?.distanceKm?:summary.bestCycle?.distanceKm?:0.0,"BEST")
        }
        projection?.let{
            Text(
                (if(it.deltaToBestKm>=0)"▲ " else "▼ ")+"%+.1f km vs Best".format(it.deltaToBestKm),
                color=if(it.deltaToBestKm>=0)secondary else Color(0xFFFFD166),
                fontSize=11.sp,
                fontWeight=FontWeight.Bold,
                modifier=Modifier.padding(top=3.dp),
            )
        }

        Spacer(Modifier.weight(1f))
        if(trackingStatus==TrackingStatus.WAITING_FOR_GPS){
            Text("Đang chờ vị trí GPS đầu tiên. Hãy ra nơi thoáng và bật Vị trí.",fontSize=12.sp,color=Color(0xFF8496A7))
        }else if(trackingStatus==TrackingStatus.PERMISSION_REQUIRED){
            Text("Cần quyền vị trí để ghi lại chuyến đi.",fontSize=12.sp,color=Color(0xFF8496A7))
        }else if(trackingStatus==TrackingStatus.ERROR){
            Text("Không khởi động được GPS. Kiểm tra quyền Vị trí rồi thử lại.",fontSize=12.sp,color=Color(0xFFFFD166))
        }
        }
    }
}

@Composable
private fun LandscapeDashboardContent(
    drive:DriveSnapshot,
    reference:EconomyReference?,
    summary:FuelSummary,
    projection:EconomyProjection?,
    trackingStatus:TrackingStatus,
    template:DashboardTemplateEntity?,
    vehicleOdometerKm:Double?,
    statusLabel:String,
    gaugeActionLabel:String,
    gaugeActionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    onGaugeClick:()->Unit,
    maxWidth:Dp,
    maxHeight:Dp,
){
    val gaugeSize=minOf(260.dp,maxHeight*.82f,maxWidth*.42f)
    val chartWidth=(maxWidth-gaugeSize-40.dp).coerceAtLeast(100.dp)
    Column(Modifier.fillMaxSize().padding(horizontal=14.dp,vertical=6.dp)){
        Row(Modifier.fillMaxWidth().padding(end=52.dp),horizontalArrangement=Arrangement.SpaceBetween){
            Text(statusLabel,color=if(trackingStatus==TrackingStatus.LIVE)secondary else Color(0xFF708295),fontSize=11.sp)
            Text(if(drive.traffic)"TRAFFIC" else (template?.name?:"TFT Sport Bike"),color=Color(0xFF8496A7),fontSize=10.sp)
            Text("GPS "+drive.rawGpsSpeedKmh.roundToInt(),color=Color(0xFF8496A7),fontSize=10.sp)
        }
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
                verticalArrangement=Arrangement.SpaceBetween,
                horizontalAlignment=Alignment.CenterHorizontally,
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
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
                    InstrumentMetric(vehicleOdometerKm,"ODO")
                    InstrumentMetric(drive.distanceKm,"TRIP")
                }
                if(drive.traffic){
                    Text("STOP/GO",color=Color(0xFFFFD166),fontWeight=FontWeight.Bold,fontSize=12.sp)
                }else drive.ecoTargetKmh?.let{target->
                    Text(
                        "Δ ECO "+"%+.0f".format(drive.displaySpeedKmh-target)+" km/h",
                        color=secondary,fontWeight=FontWeight.Bold,fontSize=12.sp,
                    )
                }
                EconomyGapChart(drive.speedHistoryKmh,drive.ecoTargetKmh,Modifier.width(chartWidth),chartHeight=48.dp)
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
                        Metric(summary.currentCycleKm,"CURRENT")
                        Metric(projection?.projectedCycleKm?:0.0,"PROJECTED")
                        Metric(reference?.cycle?.distanceKm?:summary.bestCycle?.distanceKm?:0.0,"BEST")
                    }
                    projection?.let{
                        Text(
                            (if(it.deltaToBestKm>=0)"▲ " else "▼ ")+"%+.1f km vs Best".format(it.deltaToBestKm),
                            color=if(it.deltaToBestKm>=0)secondary else Color(0xFFFFD166),
                            fontSize=10.sp,fontWeight=FontWeight.Bold,
                        )
                    }
                }
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    when(trackingStatus){
                        TrackingStatus.WAITING_FOR_GPS->Text("Đang chờ GPS · ra nơi thoáng",fontSize=11.sp,color=Color(0xFF8496A7))
                        TrackingStatus.PERMISSION_REQUIRED->Text("Cần quyền vị trí",fontSize=11.sp,color=Color(0xFF8496A7))
                        TrackingStatus.ERROR->Text("Không khởi động được GPS",fontSize=11.sp,color=Color(0xFFFFD166))
                        else->Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(value:Double,label:String){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Text(if(value>0)"%.1f".format(value) else "—",fontSize=18.sp,fontWeight=FontWeight.Bold)
        Text(label+" KM",fontSize=8.sp,color=Color(0xFF6F8192),letterSpacing=1.sp)
    }
}


@Composable
private fun InstrumentMetric(value:Double?,label:String,unit:String="km"){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        Text(value?.let{"%.1f".format(it)}?:"—",fontSize=17.sp,fontWeight=FontWeight.Bold)
        Text(label+" "+unit,fontSize=8.sp,color=Color(0xFF6F8192),letterSpacing=.7.sp)
    }
}
