package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.HomeShell
import com.example.ui.PlannerViewModel
import com.example.ui.PlannerViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CompassSensor
import com.example.util.LocationHelper
import com.example.util.NetworkMonitor

class MainActivity : ComponentActivity() {

    private val viewModel: PlannerViewModel by viewModels {
        val app = application as SmartPlannerApplication
        PlannerViewModelFactory(
            repository = app.repository,
            networkMonitor = NetworkMonitor(applicationContext),
            locationHelper = LocationHelper(applicationContext),
            compassSensor = CompassSensor(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeShell(viewModel = viewModel)
                }
            }
        }
    }
}
