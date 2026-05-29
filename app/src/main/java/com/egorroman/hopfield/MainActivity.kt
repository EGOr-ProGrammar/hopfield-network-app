package com.egorroman.hopfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.egorroman.hopfield.presentation.MainScreen
import com.egorroman.hopfield.presentation.theme.HopfieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HopfieldTheme {
                Surface {
                    MainScreen()
                }
            }
        }
    }
}