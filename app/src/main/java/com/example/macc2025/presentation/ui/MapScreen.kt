package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.macc2025.presentation.viewmodel.MapViewModel
import com.example.macc2025.presentation.ui.AppTopBar
import com.example.macc2025.presentation.ui.AppBottomBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapScreen(
    navController: NavController,
    lat: Double,
    lng: Double,
    viewModel: MapViewModel
) {
    val target = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target, 15f)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Map") },
        bottomBar = { AppBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("camera") }) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Open Camera")
            }
        }
    ) { inner ->
        GoogleMap(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = target),
                title = "Destination"
            )
        }
    }
}
