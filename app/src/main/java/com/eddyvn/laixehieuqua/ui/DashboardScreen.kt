package com.eddyvn.laixehieuqua.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.composables.icons.tabler.Tabler
import com.composables.icons.tabler.outline.GasStation
import com.eddyvn.laixehieuqua.data.DashboardTemplateEntity
import com.eddyvn.laixehieuqua.data.WeatherState
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
    weather:WeatherState,
    onRefreshWeather:()->Unit,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    onStart:()->Unit,
    onStop:()->Unit,
){
    val accent=Color(template?.accentHex?:0xFF7EEBFF)
    val secondary=Color(template?.secondaryHex?:0xFF7CFFC0)
    val speedTintTarget=runningMetricColor(
        (drive.displaySpeedKmh/120.0).toFloat().coerceIn(0f,1f),accent,secondary,
    )
    val speedTint by animateColorAsState(
        targetValue=speedTintTarget,
        animationSpec=tween(520,easing=FastOutSlowInEasing),
        label="speed-gauge-tint",
    )
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

    if(isSportCockpitLayout(template?.layoutType)){
        SportCockpitDashboard(
            drive=drive,
            trackingStatus=trackingStatus,
            vehicleOdometerKm=vehicleOdometerKm,
            tripMeterKm=tripMeterKm,
            sessionElapsedMs=displayedSessionElapsedMs,
            fuelSummary=fuelSummary,
            weather=weather,
            accent=accent,
            secondary=secondary,
            onRefreshWeather=onRefreshWeather,
            onOdoClick=onOdoClick,
            onFuelClick=onFuelClick,
            onResetTrip=onResetTrip,
            onStart=onStart,
            onStop=onStop,
        )
    }else BoxWithConstraints(Modifier.fillMaxSize()){
        if(maxWidth>maxHeight){
            LandscapeDashboardContent(
                drive=drive,
                vehicleOdometerKm=vehicleOdometerKm,
                tripMeterKm=tripMeterKm,
                sessionElapsedMs=displayedSessionElapsedMs,
                fuelSummary=fuelSummary,
                weather=weather,
                onRefreshWeather=onRefreshWeather,
                onResetTrip=onResetTrip,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                statusLabel=statusLabel,
                gaugeActionLabel=gaugeActionLabel,
                gaugeActionEnabled=gaugeClickEnabled,
                accent=accent,
                secondary=secondary,
                speedAccent=speedTint,
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
                weather=weather,
                onRefreshWeather=onRefreshWeather,
                onResetTrip=onResetTrip,
                onOdoClick=onOdoClick,
                onFuelClick=onFuelClick,
                statusLabel=statusLabel,
                gaugeActionLabel=gaugeActionLabel,
                gaugeActionEnabled=gaugeClickEnabled,
                accent=accent,
                secondary=secondary,
                speedAccent=speedTint,
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
    weather:WeatherState,
    onRefreshWeather:()->Unit,
    onResetTrip:()->Unit,
    onOdoClick:()->Unit,
    onFuelClick:()->Unit,
    statusLabel:String,
    gaugeActionLabel:String,
    gaugeActionEnabled:Boolean,
    accent:Color,
    secondary:Color,
    speedAccent:Color,
    onGaugeClick:()->Unit,
){
    Column(
        Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=10.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
    ){
        DashboardHeader(
            statusLabel,trackingStatus,template,drive.rawGpsSpeedKmh,secondary,onFuelClick
        )
        WeatherDateStrip(weather,onRefreshWeather)

        SportSpeedGauge(
            speedKmh=drive.displaySpeedKmh,
            averageSpeedKmh=drive.averageSpeedKmh,
            ecoTargetKmh=drive.ecoTargetKmh,
            accent=speedAccent,
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


internal fun isSportCockpitLayout(layoutType:String?):Boolean=
    layoutType=="SPORT_COCKPIT_V1"
