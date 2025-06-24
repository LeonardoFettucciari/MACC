package com.example.macc2025.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val orientation by viewModel.currentOrientation.collectAsState()
    val lockedOrientation by viewModel.lockedOrientation.collectAsState()
    val difference by viewModel.difference.collectAsState()

    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionsGranted =
            (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) &&
            (perms[Manifest.permission.CAMERA] == true)
    }

    LaunchedEffect(Unit) {
        val locationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        permissionsGranted = locationGranted && cameraGranted

        if (!permissionsGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Camera") }) }
    ) { innerPadding ->
        if (permissionsGranted) {
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
                val sensorManager =
                    context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientations = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientations)

                        val yaw = Math.toDegrees(orientations[0].toDouble()).toFloat()
                        val pitch = Math.toDegrees(orientations[1].toDouble()).toFloat()
                        val roll = Math.toDegrees(orientations[2].toDouble()).toFloat()

                        val orientationDeg = if (kotlin.math.abs(pitch) > 60f) {
                            (yaw + 360f) % 360f
                        } else {
                            (roll + 360f) % 360f
                        }
                    }
            ) {
                AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
                val displayText = when {
                    difference != null -> "Off by ${difference!!.roundToInt()}°"
                    lockedOrientation != null -> "Locked: ${lockedOrientation?.roundToInt()}°"
                    else -> "Orientation: ${orientation.roundToInt()}°"
                }

                        viewModel.updateOrientation(orientationDeg)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                sensorManager.registerListener(
                    listener,
                    rotationVector,
                    SensorManager.SENSOR_DELAY_UI
                )
                onDispose { sensorManager.unregisterListener(listener) }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch {
                                fusedClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        viewModel.setCurrentLocation(
                                            LatLng(it.latitude, it.longitude)
                                        )
                                        viewModel.lockOrientation()
                                    }
                                }

                            }
                        }
                    }
            ) {
                AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
                val (displayText, textColor) = when {
                    difference != null -> {
                        val color = if (difference!! < 15f) Color.Green else Color.Red
                        "Off by ${difference!!.roundToInt()}°" to color
                    }
                    lockedOrientation != null -> {
                        "Locked: ${lockedOrientation?.roundToInt()}°" to Color.White
                    }
                    else -> {
                        "Orientation: ${orientation.roundToInt()}°" to Color.White
                    }
                }

                Text(
                    text = displayText,
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
                if (difference != null) {
                    Button(
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
                        Text("Search Again")
                    }
                }
        ) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
            val displayText = when {
                difference != null -> "Off by ${difference!!.roundToInt()}°"
                lockedOrientation != null -> "Locked: ${lockedOrientation?.roundToInt()}°"
                else -> "Orientation: ${orientation.roundToInt()}°"
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera and location permissions are required to use this feature.",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera and location permissions are required to use this feature.",
                color = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            )
        }
    }
}

