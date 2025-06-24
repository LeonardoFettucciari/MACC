package com.example.macc2025.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.maps.model.LatLng

class MainViewModel : ViewModel() {
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    private val _currentOrientation = MutableStateFlow(0f)
    val currentOrientation: StateFlow<Float> = _currentOrientation

    private val _lockedOrientation = MutableStateFlow<Float?>(null)
    val lockedOrientation: StateFlow<Float?> = _lockedOrientation

    fun setSelectedLocation(location: LatLng) {
        _selectedLocation.value = location
    }

    fun updateOrientation(degrees: Float) {
        _currentOrientation.value = degrees
    }

    fun lockOrientation() {
        _lockedOrientation.value = _currentOrientation.value
    }

    fun reset() {
        _selectedLocation.value = null
        _lockedOrientation.value = null
    }
}
