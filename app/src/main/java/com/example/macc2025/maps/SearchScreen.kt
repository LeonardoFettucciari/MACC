package com.example.macc2025.maps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    placesClient: PlacesClient,
    viewModel: MainViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    fun search(text: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(text)
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
            }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Search") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
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

            LazyColumn {
                items(predictions) { prediction ->
                    Text(
                        text = prediction.getFullText(null).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val placeRequest = FetchPlaceRequest.builder(
                                    prediction.placeId,
                                    listOf(Place.Field.LAT_LNG)
                                ).build()
                                placesClient.fetchPlace(placeRequest)
                                    .addOnSuccessListener { placeResponse ->
                                        placeResponse.place.latLng?.let { latLng ->
                                            viewModel.setSelectedLocation(latLng)
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
