package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc2025.presentation.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val points = viewModel.points.collectAsState()
    val usernameState = viewModel.username.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Profile") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("User: ${user?.email ?: ""}")
            Text("Total points: ${points.value}")
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = newName.takeIf { it.isNotEmpty() } ?: (usernameState.value ?: ""),
                onValueChange = { newName = it },
                label = { Text("Username") }
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val n = newName.ifBlank { usernameState.value ?: "" }
                if (n.isNotBlank()) {
                    viewModel.updateUsername(n)
                    newName = ""
                }
            }) { Text("Save") }
        }
    }
}
