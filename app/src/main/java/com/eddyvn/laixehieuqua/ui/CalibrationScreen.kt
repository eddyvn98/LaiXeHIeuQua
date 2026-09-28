package com.eddyvn.laixehieuqua.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eddyvn.laixehieuqua.camera.SpeedOcrAnalyzer

@Composable
fun CalibrationScreen(vm:MainViewModel){
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current
    val drive by vm.drive.collectAsStateWithLifecycle()
    val ocr by vm.lastOcr.collectAsStateWithLifecycle()
    val status by vm.calibrationStatus.collectAsStateWithLifecycle()

    var granted by remember{
        mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
    }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted=it}

    Column(Modifier.fillMaxSize().padding(16.dp)){
        Text("Camera speed sync",style=MaterialTheme.typography.headlineSmall)
        Text("Center the real speedometer digits in the camera. Stable GPS and OCR samples are paired automatically.")

        if(!granted){
            Button({launcher.launch(Manifest.permission.CAMERA)},modifier=Modifier.padding(top=12.dp)){Text("ALLOW CAMERA")}
        }else{
            val analyzer=remember{SpeedOcrAnalyzer(vm::onOcrSpeed)}
            var provider by remember{mutableStateOf<ProcessCameraProvider?>(null)}

            AndroidView(
                factory={ctx->
                    PreviewView(ctx).also{view->
                        val future=ProcessCameraProvider.getInstance(ctx)
                        future.addListener({
                            val p=future.get()
                            provider=p
                            val preview=Preview.Builder().build().also{it.surfaceProvider=view.surfaceProvider}
                            val analysis=ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                            analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx),analyzer)
                            p.unbindAll()
                            p.bindToLifecycle(lifecycle,CameraSelector.DEFAULT_BACK_CAMERA,preview,analysis)
                        },ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier=Modifier.fillMaxWidth().height(360.dp).padding(top=12.dp),
            )
            DisposableEffect(Unit){onDispose{provider?.unbindAll()}}
        }

        Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.SpaceBetween){
            Text("GPS/Fused: %.1f km/h".format(drive.trueSpeedKmh))
            Text("Camera: "+(ocr?.toString()?:"—")+" km/h")
        }
        Text(status,modifier=Modifier.padding(top=8.dp))
        Text("Calibration samples are stored only after several stable frames. Do not interact with the phone while riding.",modifier=Modifier.padding(top=8.dp))
    }
}
