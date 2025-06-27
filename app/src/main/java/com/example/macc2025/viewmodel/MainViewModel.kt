package com.example.macc2025.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc2025.domain.repository.UserRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _currentOrientation = MutableStateFlow(0f)
    val currentOrientation: StateFlow<Float> = _currentOrientation

    private val _lockedOrientation = MutableStateFlow<Float?>(null)
    val lockedOrientation: StateFlow<Float?> = _lockedOrientation

    private val _difference = MutableStateFlow<Float?>(null)
    val difference: StateFlow<Float?> = _difference

    private val _points = MutableStateFlow<Int?>(null)
    val points: StateFlow<Int?> = _points

    private val _bearing = MutableStateFlow<Float?>(null)
    val bearing: StateFlow<Float?> = _bearing

    fun updateOrientation(azimuth: Float) {
        _currentOrientation.value = azimuth
    }

    fun lockOrientation() {
        _lockedOrientation.value = _currentOrientation.value
        computeDifference()
    }

    private fun computeDifference(start: LatLng, end: LatLng, locked: Float) {
        val bearing = calculateBearing(start, end)
        _bearing.value = bearing
        val diff = (bearing - locked + 360) % 360
        val finalDiff = if (diff > 180) 360 - diff else diff
        _difference.value = finalDiff

        val points = calculatePoints(finalDiff)
        _points.value = points
        val uid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
        if (uid != null) {
            viewModelScope.launch {
                try {
                    userRepository.addPoints(uid, points)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun calculatePoints(diff: Float): Int {
        if (diff > 50f) return 0
        val k = ln(50.0) / 50.0
        return (500 * exp(-k * diff)).toInt()
    }

    fun reset() {
        _lockedOrientation.value = null
        _difference.value = null
        _points.value = null
        _bearing.value = null
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
