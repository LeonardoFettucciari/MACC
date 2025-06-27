package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameScreen(navController: NavController, viewModel: ProfileViewModel) {
    var username by remember { mutableStateOf("") }

    Scaffold(topBar = { AppTopBar(title = "Choose Username") }) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (username.isNotBlank()) {
                    viewModel.updateUsername(username)
                    navController.navigate("search") {
                        popUpTo("username") { inclusive = true }
                    }
                }
            }) { Text("Continue") }
        }
    }
}
