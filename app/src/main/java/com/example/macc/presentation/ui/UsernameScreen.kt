package com.example.macc.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc.presentation.viewmodel.ProfileViewModel
import com.example.macc.presentation.ui.AppTopBar

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
            ElevatedCard {
                Column(modifier = Modifier.padding(16.dp)) {
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
    }
}
