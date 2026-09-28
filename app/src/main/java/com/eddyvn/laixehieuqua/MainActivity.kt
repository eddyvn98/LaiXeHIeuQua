package com.eddyvn.laixehieuqua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eddyvn.laixehieuqua.ui.AppRoot
import com.eddyvn.laixehieuqua.ui.LaiXeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LaiXeTheme { AppRoot() } }
    }
}
