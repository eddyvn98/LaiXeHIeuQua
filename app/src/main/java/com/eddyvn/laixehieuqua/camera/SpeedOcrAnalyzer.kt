package com.eddyvn.laixehieuqua.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

class SpeedOcrAnalyzer(private val onSpeed:(Int)->Unit):ImageAnalysis.Analyzer{
    private val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val busy=AtomicBoolean(false)

    override fun analyze(imageProxy:ImageProxy){
        if(!busy.compareAndSet(false,true)){imageProxy.close();return}
        val media=imageProxy.image
        if(media==null){busy.set(false);imageProxy.close();return}

        val width=imageProxy.width.toDouble()
        val height=imageProxy.height.toDouble()
        val centerX=width/2.0
        val centerY=height/2.0
        val maxDistance=hypot(centerX,centerY).coerceAtLeast(1.0)

        val input=InputImage.fromMediaImage(media,imageProxy.imageInfo.rotationDegrees)
        recognizer.process(input)
            .addOnSuccessListener{result->
                val candidate=result.textBlocks
                    .flatMap{it.lines}
                    .flatMap{it.elements}
                    .mapNotNull{element->
                        val digits=element.text.filter(Char::isDigit)
                        val speed=digits.toIntOrNull()
                        val box=element.boundingBox
                        if(speed==null||speed !in 0..199||box==null)return@mapNotNull null

                        val dx=box.exactCenterX()-centerX
                        val dy=box.exactCenterY()-centerY
                        val normalizedDistance=(hypot(dx,dy)/maxDistance).coerceIn(0.0,1.0)
                        if(normalizedDistance>0.70)return@mapNotNull null

                        val area=(box.width()*box.height()).toDouble().coerceAtLeast(1.0)
                        val score=area*(1.0-normalizedDistance*0.72)
                        speed to score
                    }
                    .maxByOrNull{it.second}
                    ?.first
                candidate?.let(onSpeed)
            }
            .addOnCompleteListener{
                busy.set(false)
                imageProxy.close()
            }
    }
}
