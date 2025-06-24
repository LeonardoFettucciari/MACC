package com.example.macc2025.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CameraScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val orientation by viewModel.currentOrientation.collectAsState()
    val lockedOrientation by viewModel.lockedOrientation.collectAsState()
    val difference by viewModel.difference.collectAsState()

    val previewView = remember { PreviewView(context) }
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview)
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientations = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientations)
                val azimuthRad = orientations[0]
                val degrees = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360) % 360
                viewModel.updateOrientation(degrees)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    scope.launch {
                        fusedClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                viewModel.setCurrentLocation(LatLng(it.latitude, it.longitude))
                                viewModel.lockOrientation()
                            }
                        }
                    }
                }
            }
    ) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        val displayText = when {
            difference != null -> "Off by ${difference!!.roundToInt()}°"
            lockedOrientation != null -> "Locked: ${lockedOrientation?.roundToInt()}°"
            else -> "Orientation: ${orientation.roundToInt()}°"
        }
        Text(
            text = displayText,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp)
        )
        if (difference != null) {
            androidx.compose.material3.Button(
                onClick = {
                    viewModel.reset()
                    navController.navigate("search") {
                        popUpTo("search") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                androidx.compose.material3.Text("Search Again")
            }
        }
    }
}

