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

    fun updateOrientation(degrees: Float) {
        _currentOrientation.value = degrees
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
            val startLoc = android.location.Location("start").apply {
                latitude = start.latitude
                longitude = start.longitude
            }
            val endLoc = android.location.Location("end").apply {
                latitude = end.latitude
                longitude = end.longitude
            }
            var bearing = startLoc.bearingTo(endLoc)
            if (bearing < 0) bearing += 360f
            var diff = kotlin.math.abs(locked - bearing)
            if (diff > 180f) diff = 360f - diff
            _difference.value = diff
        }
    }

    fun reset() {
        _selectedLocation.value = null
        _lockedOrientation.value = null
        _currentLocation.value = null
        _difference.value = null
    }
}
