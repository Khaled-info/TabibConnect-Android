package com.example.tabibconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tabibconnect.ui.navigation.TabibConnectApp
import com.example.tabibconnect.ui.theme.TabibConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabibConnectTheme {
                TabibConnectApp()
            }
        }
    }
}
