package com.example.tmaadu4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OsmLocationMapScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsmLocationMapScreen() {

    val context = LocalContext.current  // získání aktuálního kontextu
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) } //zjištění lokace, wifi atd, rememebr -> hodnota se vytovří jenom jednou znovu se nepočítá

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName        // nastaví useragenta pro map server
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(      //ptáme se na oprávnění
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED // ověříme že je svolení dáno
        )
    }

    var latText by remember { mutableStateOf("—") }
    var lonText by remember { mutableStateOf("—") }
    var geoPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult( //vykreslí dialgoové okno
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    DisposableEffect(hasPermission) { // Pro spuštění a ukončnení sledování polohy

        if (!hasPermission) return@DisposableEffect onDispose { } //když nemá permision nedělá nic

        val request = LocationRequest.Builder(  //říkáme si jak rychle chceme dostávat aktualziace polohy a přesnost
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) { //když dostane telefon novou polohu, tak se to zavolá

                val loc = result.lastLocation ?: return //používáme poslední známou lokaci, pokud není tak skončíme

                latText = "%.6f".format(loc.latitude)
                lonText = "%.6f".format(loc.longitude)

                geoPoint = GeoPoint(loc.latitude, loc.longitude) //objekt pro samotnou mapu, aby věděla kde jsme
            }
        }

        try {
            fused.requestLocationUpdates(request, callback, Looper.getMainLooper()) // říkáme systému aby nám posiílal průběžně nové souřadince, try catch proti pádu
        } catch (_: SecurityException) {}

        onDispose {
            fused.removeLocationUpdates(callback)   //když přestanu používat obrazovku přestanu používat gps
        }
    }

    if (!hasPermission) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.coordinates_empty)) }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(stringResource(R.string.permission_needed))

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    ) {
                        Text(stringResource(R.string.allow_location))
                    }
                }
            }
        }

        return
    }

    if (geoPoint == null) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.coordinates_empty)) }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.waiting_for_gps))
            }
        }

        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.coordinates_title,
                            latText,
                            lonText
                        )
                    )
                }
            )
        }
    ) { padding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            factory = { ctx ->      //tvorba mapy

                org.osmdroid.views.MapView(ctx).apply {

                    setMultiTouchControls(true)

                    controller.setZoom(17.0)
                    controller.setCenter(geoPoint!!)    // !! -> určitě to není null

                    val marker = Marker(this).apply {

                        position = geoPoint!!
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        title = context.getString(R.string.marker_title)
                    }

                    overlays.add(marker)
                }
            },

            update = { mapView ->       //překreslování mapy

                mapView.controller.setCenter(geoPoint!!)

                val marker =
                    mapView.overlays.firstOrNull { it is Marker } as? Marker

                if (marker != null) {

                    marker.position = geoPoint!!
                    mapView.invalidate()
                }
            }
        )
    }
}