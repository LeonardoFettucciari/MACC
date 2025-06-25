package com.example.macc2025.maps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel
) {
    var query by remember { mutableStateOf("") }
    val predictions by viewModel.predictions.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

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
                    viewModel.search(it)
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
                                viewModel.fetchPlace(p.placeId) {
                                    navController.navigate("camera")
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
