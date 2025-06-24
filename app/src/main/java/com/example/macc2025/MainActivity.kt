package com.example.macc2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.macc2025.navigation.AppNavHost
import com.example.macc2025.presentation.ui.theme.MACC2025Theme
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : ComponentActivity() {
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // init Places SDK
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        setContent {
            MACC2025Theme {
                AppNavHost(placesClient)
            }
        }
    }
}
