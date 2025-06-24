package com.example.macc2025.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _currentOrientation = MutableStateFlow(0f)
    val currentOrientation: StateFlow<Float> = _currentOrientation

    private val _lockedOrientation = MutableStateFlow<Float?>(null)
    val lockedOrientation: StateFlow<Float?> = _lockedOrientation

    private val _difference = MutableStateFlow<Float?>(null)
    val difference: StateFlow<Float?> = _difference

    fun setSelectedLocation(location: LatLng) {
        _selectedLocation.value = location
    }

    fun setCurrentLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun updateOrientation(azimuth: Float) {
        _currentOrientation.value = azimuth
    }

    fun lockOrientation() {
        _lockedOrientation.value = _currentOrientation.value
        computeDifference()
    }

    private fun computeDifference() {
        val start = _currentLocation.value
        val end = _selectedLocation.value
        val locked = _lockedOrientation.value
        if (start != null && end != null && locked != null) {
            val bearing = calculateBearing(start, end)
            _difference.value = (bearing - locked + 360) % 360
        }
    }

    fun reset() {
        _lockedOrientation.value = null
        _difference.value = null
    }

    private fun calculateBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)
        val dLon = lon2 - lon1

        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        val bearing = Math.toDegrees(Math.atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }
}
