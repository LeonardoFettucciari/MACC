package com.example.macc2025.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapScreen(lat: Double, lng: Double) {
    val position = LatLng(lat, lng)
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    GoogleMap(
        modifier = Modifier,
        cameraPositionState = cameraPositionState
    ) {
        Marker(position = position)
    }
}
