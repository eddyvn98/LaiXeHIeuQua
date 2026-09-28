package com.eddyvn.laixehieuqua.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

class SpeedOcrAnalyzer(private val onSpeed:(Int)->Unit):ImageAnalysis.Analyzer{
    private val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val busy=AtomicBoolean(false)

    override fun analyze(imageProxy:ImageProxy){
        if(!busy.compareAndSet(false,true)){imageProxy.close();return}
        val media=imageProxy.image
        if(media==null){busy.set(false);imageProxy.close();return}
        val input=InputImage.fromMediaImage(media,imageProxy.imageInfo.rotationDegrees)
        recognizer.process(input)
            .addOnSuccessListener{result->
                val candidate=result.textBlocks.flatMap{it.lines}.flatMap{it.elements}
                    .mapNotNull{element->
                        val speed=element.text.filter(Char::isDigit).toIntOrNull()
                        val box=element.boundingBox
                        if(speed!=null&&speed in 0..199&&box!=null)speed to (box.width()*box.height()) else null
                    }.maxByOrNull{it.second}?.first
                candidate?.let(onSpeed)
            }
            .addOnCompleteListener{busy.set(false);imageProxy.close()}
    }
}
