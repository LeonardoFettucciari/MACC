package com.example.macc2025.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen(
    navController: NavController,
    lat: Double,
    lng: Double,
    viewModel: MainViewModel = viewModel()
) {
    val location = LatLng(lat, lng)
    viewModel.setSelectedLocation(location)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }
    val markerState = rememberMarkerState(position = location)

    GoogleMap(
        modifier = Modifier,
        cameraPositionState = cameraPositionState
    ) {
        Marker(state = markerState)
    }
    Button(onClick = { navController.navigate("camera") }) {
        Text("Open Camera")
    }
}
