package com.example.pilab

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pilab.ui.PilabApp
import com.example.pilab.ui.theme.PilabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PilabTheme {
                PilabApp()
            }
        }
    }
}
