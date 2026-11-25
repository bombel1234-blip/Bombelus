package com.mojdzien.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mojdzien.app.ui.MainRoot
import com.mojdzien.app.ui.theme.MyDayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDayTheme {
                val vm: MainViewModel = viewModel()
                MainRoot(vm)
            }
        }
    }
}
