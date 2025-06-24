package com.example.macc2025.maps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    placesClient: PlacesClient,
    viewModel: MainViewModel
) {
    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    fun search(text: String) {
        if (text.isBlank()) {
            predictions = emptyList()
            return
        }
        val req = FindAutocompletePredictionsRequest.builder()
            .setQuery(text)
            .build()
        placesClient.findAutocompletePredictions(req)
            .addOnSuccessListener { resp ->
                predictions = resp.autocompletePredictions
                errorMessage = null
            }
            .addOnFailureListener { err ->
                predictions = emptyList()
                errorMessage = err.localizedMessage
            }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Search") }) }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    search(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search location") }
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn {
                items(predictions) { p ->
                    Text(
                        text = p.getFullText(null).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val fr = FetchPlaceRequest.builder(
                                    p.placeId,
                                    listOf(Place.Field.LAT_LNG)
                                ).build()
                                placesClient.fetchPlace(fr)
                                    .addOnSuccessListener { r ->
                                        r.place.latLng?.let { ll ->
                                            viewModel.setSelectedLocation(ll)
                                            navController.navigate("camera")
                                        }
                                    }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
