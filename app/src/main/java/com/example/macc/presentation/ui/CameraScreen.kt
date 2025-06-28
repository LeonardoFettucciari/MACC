package com.example.macc.presentation.ui

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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import kotlin.math.absoluteValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.macc.presentation.viewmodel.CameraViewModel
import com.example.macc.presentation.viewmodel.SearchViewModel
import com.example.macc.utils.camera.getCameraProvider
import com.example.macc.presentation.ui.AppBottomBar
import com.example.macc.utils.graphics.CircumflexIcon
import com.example.macc.utils.graphics.HorizonTicks
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import org.opencv.android.OpenCVLoader
import org.opencv.core.Scalar
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import com.example.macc.presentation.ui.components.RichTooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    searchViewModel: SearchViewModel,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val orientation by viewModel.currentOrientation.collectAsState()
    val difference by viewModel.difference.collectAsState(initial = null)
    val points by viewModel.points.collectAsState()
    val bearing by viewModel.bearing.collectAsState()
    var isUpright by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        OpenCVLoader.initDebug()
    }

    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val camOk = perms[Manifest.permission.CAMERA] == true
        val locOk = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        permissionsGranted = camOk && locOk
    }
    LaunchedEffect(Unit) {
        val camOk = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val locOk = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        permissionsGranted = camOk && locOk
        if (!permissionsGranted) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }
    }

    Scaffold(
        topBar = {
            RichTooltip(
                prefKey = "camera_tooltip",
                richTooltipSubheadText = "Guess the place direction",
                richTooltipText = "Nice choice! Now turn around and try guessing where the place is. When you think you got it, tap the screen!"

            ) {
                AppTopBar(title = "Camera")
            }
        },
        bottomBar = { AppBottomBar(navController) }
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
        val arrowBitmap = remember { CircumflexIcon.create(200).asImageBitmap() }
        val correctArrowBitmap = remember {
            CircumflexIcon.create(200, Scalar(0.0, 255.0, 0.0, 255.0)).asImageBitmap()
        }
        val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

        LaunchedEffect(permissionsGranted) {
            if (!permissionsGranted) return@LaunchedEffect
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
                        Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                            raw,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            remap
                        )

                        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                            raw,
                            SensorManager.AXIS_Z,
                            SensorManager.AXIS_MINUS_X,
                            remap
                        )

                        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                            raw,
                            SensorManager.AXIS_MINUS_X,
                            SensorManager.AXIS_MINUS_Z,
                            remap
                        )

                        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                            raw,
                            SensorManager.AXIS_MINUS_Z,
                            SensorManager.AXIS_X,
                            remap
                        )

                        else -> System.arraycopy(raw, 0, remap, 0, 9)
                    }
                    val o = FloatArray(3)
                    SensorManager.getOrientation(remap, o)
                    val az = ((Math.toDegrees(o[0].toDouble()) + 360) % 360).toFloat()
                    val pitch = Math.toDegrees(o[1].toDouble()).toFloat()
                    val roll = Math.toDegrees(o[2].toDouble()).toFloat()
                    isUpright = pitch.absoluteValue <= 30f && roll.absoluteValue <= 30f

                    viewModel.updateOrientation(az)
                }
            }
            sm.registerListener(listener, rotVec, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }

        val handleTap = handle@{
            if (viewModel.lockedOrientation.value != null) return@handle
            fun onLoc(loc: Location) {
                searchViewModel.selectedLocation.value?.let { dest ->
                    viewModel.lockOrientation(
                        LatLng(loc.latitude, loc.longitude),
                        dest
                    )
                }
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

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.roundToPx() }
            val ticksBitmap = remember(orientation, widthPx) {
                HorizonTicks.create(orientation.roundToInt(), widthPx).asImageBitmap()
            }

            // CAMERA PREVIEW LAYER
            AndroidView(
                factory = {
                    previewView.apply {
                        setOnTouchListener { _, ev ->
                            if (ev.action == MotionEvent.ACTION_UP && isUpright) handleTap()
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // HUD & OVERLAYS
            Box(
                Modifier
                    .fillMaxSize()
                    .let { if (!isUpright) it.blur(16.dp) else it }
            ) {
                Image(
                    bitmap = ticksBitmap,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )

                Image(
                    bitmap = arrowBitmap,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )

                bearing?.let { brg ->
                    val spacing = widthPx / 60f
                    var d = (brg - orientation + 360f) % 360f
                    if (d > 180f) d -= 360f
                    val clamped = d.coerceIn(-30f, 30f)

                    Image(
                        bitmap = correctArrowBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer { translationX = clamped * spacing }
                    )
                }

                val (label, color) = difference?.let { diff ->
                    val pts = points
                    val c = if (diff < 15f) Color.Green else Color.Red
                    "Off by ${diff.roundToInt()}° - $pts pts" to c
                } ?: ("Heading: ${orientation.roundToInt()}°" to Color.White)

                Text(
                    label,
                    color = color,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )
            }

            // START OVER BUTTON WITH TOOLTIP
            if (difference != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    RichTooltip(
                        prefKey = "startover_tooltip",
                        richTooltipSubheadText = "Search a new location",
                        richTooltipText = "Great job! If you want to try other locations, just tap this button and start searching for new places!"
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.reset()
                                navController.navigate("search") {
                                    popUpTo("search") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text("Start Over")
                        }
                    }
                }
            }

            // UPRIGHT OVERLAY (BLOCK UI IF TILTED)
            if (!isUpright) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Hold the device upright",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(24.dp)
                    )
                }
            }
        }

    }
}

