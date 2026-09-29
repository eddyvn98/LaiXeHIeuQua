package com.eddyvn.laixehieuqua.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs
import kotlin.math.max

@Composable
fun DriveAmbientBackground(
    speedKmh:Double,
    accelerationMs2:Double,
    modifier:Modifier=Modifier,
){
    val speedFactor by animateFloatAsState(
        targetValue=(speedKmh/140.0).coerceIn(0.0,1.0).toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="drive-speed-background",
    )
    val accelerationFactor by animateFloatAsState(
        targetValue=(accelerationMs2/4.0).coerceIn(-1.0,1.0).toFloat(),
        animationSpec=spring(dampingRatio=Spring.DampingRatioNoBouncy,stiffness=Spring.StiffnessLow),
        label="drive-acceleration-background",
    )

    Canvas(modifier){
        drawRect(Color(0xFF030508))

        val speedColor=if(speedFactor<0.55f){
            lerp(Color(0xFF087C9A),Color(0xFF503E9B),speedFactor/0.55f)
        }else{
            lerp(Color(0xFF503E9B),Color(0xFFC43837),((speedFactor-0.55f)/0.45f).coerceIn(0f,1f))
        }
        val radius=max(size.width,size.height)*0.82f
        drawRect(
            Brush.radialGradient(
                colors=listOf(speedColor.copy(alpha=0.28f),speedColor.copy(alpha=0.12f),Color.Transparent),
                center=Offset(size.width*0.5f,size.height*0.32f),
                radius=radius,
            )
        )

        val accelerationStrength=abs(accelerationFactor)
        if(accelerationStrength>0.015f){
            val accelerationColor=if(accelerationFactor>=0f)Color(0xFFFF5A36)else Color(0xFF00A8D6)
            val centerX=if(accelerationFactor>=0f)size.width*0.82f else size.width*0.18f
            drawRect(
                Brush.radialGradient(
                    colors=listOf(
                        accelerationColor.copy(alpha=0.25f*accelerationStrength),
                        accelerationColor.copy(alpha=0.09f*accelerationStrength),
                        Color.Transparent,
                    ),
                    center=Offset(centerX,size.height*0.68f),
                    radius=max(size.width,size.height)*0.58f,
                )
            )
        }
    }
}
