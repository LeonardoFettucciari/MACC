package com.example.macc2025.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.navigation.NavController
import com.example.macc2025.presentation.viewmodel.ProfileViewModel
import com.example.macc2025.presentation.ui.AppTopBar
import com.example.macc2025.presentation.ui.AppBottomBar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val points = viewModel.points.collectAsState()
    val usernameState = viewModel.username.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    var newName by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Profile") },
        bottomBar = { AppBottomBar(navController) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Email: ${user?.email ?: ""}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Username: ${usernameState.value ?: ""}")
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit username")
                        }
                    }
                    if (editing) {
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
                            editing = false
                        }) { Text("Save") }
                    }
                }
            }
            Text(
                text = "${points.value} points",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineLarge
            )
            FilledTonalButton(
                onClick = {
                    AuthUI.getInstance().signOut(context)
                    FirebaseAuth.getInstance().signOut()
                },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { Text("Logout") }
        }
    }
}
