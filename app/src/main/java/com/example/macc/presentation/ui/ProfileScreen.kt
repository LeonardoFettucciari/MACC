package com.example.macc.presentation.ui

import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.navigation.NavController
import com.example.macc.presentation.viewmodel.ProfileViewModel
import com.example.macc.presentation.ui.AppTopBar
import com.example.macc.presentation.ui.AppBottomBar
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation

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
    val isPasswordUser = user?.providerData?.any { it.providerId == "password" } == true
    var newName by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var editingPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "Profile") },
        bottomBar = { AppBottomBar(navController) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${points.value} points",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Email: ${user?.email ?: ""}")

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = if (editing) newName else (usernameState.value ?: ""),
                        onValueChange = { newName = it },
                        label = { Text("Username") },
                        enabled = true,
                        readOnly = !editing,
                        trailingIcon = {
                            if (editing) {
                                Row {
                                    IconButton(onClick = {
                                        val trimmed = newName.trim()
                                        if (trimmed.isBlank()) {
                                            message = "Username cannot be empty"
                                        } else {
                                            viewModel.updateUsername(trimmed)
                                            editing = false
                                            newName = ""
                                            message = "Username updated"
                                        }

                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Save username")
                                    }
                                    IconButton(onClick = {
                                        editing = false
                                        newName = ""
                                        message = null

                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                                    }
                                }
                            } else {
                                IconButton(onClick = {
                                    newName = usernameState.value ?: ""
                                    editing = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit username")
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    if (isPasswordUser) {
                        if (editingPassword) {
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Old Password") },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = if (editingPassword) newPassword else "********",
                            onValueChange = { newPassword = it },
                            label = { Text(if (editingPassword) "New Password" else "Password") },
                            enabled = true,
                            readOnly = !editingPassword,
                            visualTransformation = PasswordVisualTransformation(),
                            trailingIcon = {
                                if (editingPassword) {
                                    Row {
                                        IconButton(onClick = {
                                            if (oldPassword.isBlank() || newPassword.isBlank()) {
                                                message = "Please provide old and new password"
                                                return@IconButton
                                            }

                                            val email = user?.email ?: return@IconButton
                                            val credential = EmailAuthProvider.getCredential(email, oldPassword)
                                            user.reauthenticate(credential)
                                                .addOnSuccessListener {
                                                    user.updatePassword(newPassword)
                                                        .addOnSuccessListener {
                                                            message = "Password updated"
                                                            editingPassword = false
                                                            newPassword = ""
                                                            oldPassword = ""
                                                        }
                                                        .addOnFailureListener { e -> message = e.localizedMessage }
                                                }
                                                .addOnFailureListener { e -> message = e.localizedMessage }
                                        }) {
                                            Icon(Icons.Default.Check, contentDescription = "Save password")
                                        }
                                        IconButton(onClick = {
                                            editingPassword = false
                                            newPassword = ""
                                            oldPassword = ""
                                            message = null

                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                                        }
                                    }
                                } else {
                                    IconButton(onClick = { editingPassword = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit password")
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            FilledTonalButton(
                onClick = {
                    AuthUI.getInstance().signOut(context)
                    FirebaseAuth.getInstance().signOut()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
