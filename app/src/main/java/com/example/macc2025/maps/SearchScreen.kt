package com.example.macc2025.maps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.viewmodel.MainViewModel
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.example.macc2025.presentation.viewmodel.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.macc2025.presentation.ui.AppTopBar
import com.example.macc2025.presentation.ui.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val username by profileViewModel.username.collectAsState()
    var query by remember { mutableStateOf("") }
    val predictions by viewModel.predictions.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.search(query)
    }

    LaunchedEffect(username) {
        if (username == null) {
            navController.navigate("username")
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Search")
        },
        bottomBar = {
            AppBottomBar(navController)
        }
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
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.fetchPlace(p.placeId) {
                                    navController.navigate("camera")
                                }
                            }
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(p.getFullText(null).toString())
                            }
                        )
                    }
                }
            }
        }
    }
}
