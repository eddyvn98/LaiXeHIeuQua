package com.eddyvn.laixehieuqua.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors=darkColorScheme(
    background=Color(0xFF020407),surface=Color(0xFF070B10),
    primary=Color(0xFF5CE7FF),secondary=Color(0xFF68FFB2),
    onBackground=Color(0xFFF5F9FC),onSurface=Color(0xFFF5F9FC),
)

@Composable
fun LaiXeTheme(content:@Composable ()->Unit){
    MaterialTheme(colorScheme=DarkColors,typography=Typography(),content=content)
}
