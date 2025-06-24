package com.example.macc2025.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.view.MotionEvent
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val orientation by viewModel.currentOrientation.collectAsState()
    val difference by viewModel.difference.collectAsState(initial = null)

    var permissionsGranted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val camOk = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val locOk = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        permissionsGranted = camOk && locOk
        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Camera") }) }
    ) { innerPadding ->
        if (!permissionsGranted) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera & location permissions required.",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }
            return@Scaffold
        }

        val previewView = remember { PreviewView(context) }
        val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

        LaunchedEffect(Unit) {
            val camProvider = context.getCameraProvider()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            camProvider.unbindAll()
            camProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview
            )
        }

        DisposableEffect(Unit) {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val rotVec = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                override fun onSensorChanged(e: SensorEvent) {
                    val raw = FloatArray(9).also {
                        SensorManager.getRotationMatrixFromVector(it, e.values)
                    }
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val rot = wm.defaultDisplay.rotation
                    val remap = FloatArray(9)
                    when (rot) {
                        Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(raw, SensorManager.AXIS_X, SensorManager.AXIS_Z, remap)
                        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(raw, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remap)
                        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(raw, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Z, remap)
                        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(raw, SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X, remap)
                        else -> System.arraycopy(raw, 0, remap, 0, 9)
                    }
                    val o = FloatArray(3)
                    SensorManager.getOrientation(remap, o)
                    val az = ((Math.toDegrees(o[0].toDouble()) + 360) % 360).toFloat()
                    viewModel.updateOrientation(az)
                }
            }
            sm.registerListener(listener, rotVec, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }

        val handleTap = {
            fun onLoc(loc: Location) {
                viewModel.setCurrentLocation(LatLng(loc.latitude, loc.longitude))
                viewModel.lockOrientation()
            }
            fusedClient.lastLocation.addOnSuccessListener { l ->
                if (l != null) onLoc(l)
                else {
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { f -> f?.let { onLoc(it) } }
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = {
                    previewView.apply {
                        setOnTouchListener { _, ev ->
                            if (ev.action == MotionEvent.ACTION_UP) handleTap()
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            val diff = difference
            val (label, color) = if (diff != null) {
                val c = if (diff < 15f) Color.Green else Color.Red
                "Off by ${diff.roundToInt()}°" to c
            } else {
                "Heading: ${orientation.roundToInt()}°" to Color.White
            }

            Text(
                label,
                color = color,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            )

            diff?.let {
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
                    Text("Start Over")
                }
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
