package com.example.macc.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.macc.presentation.viewmodel.ProfileViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val points by viewModel.points.collectAsState()
    val username by viewModel.username.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val isPasswordUser = user?.providerData?.any { it.providerId == "password" } == true

    var editingUsername by rememberSaveable { mutableStateOf(false) }
    var usernameDraft by rememberSaveable { mutableStateOf("") }

    var editingPwd by rememberSaveable { mutableStateOf(false) }
    var oldPwd by rememberSaveable { mutableStateOf("") }
    var newPwd by rememberSaveable { mutableStateOf("") }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var deletePwd by rememberSaveable { mutableStateOf("") }
    var deleteError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Profile") }) },
        bottomBar = { AppBottomBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        Text(points.toString(), style = MaterialTheme.typography.displayLarge)
                        Text("points", style = MaterialTheme.typography.titleMedium)
                    }

            }

            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {


                        ListItem(
                            leadingContent = { Icon(Icons.Default.Email, null) },
                            headlineContent = { Text("Email") },
                            supportingContent = { Text(user?.email ?: "") }
                        )

                        if (editingUsername) {
                            OutlinedTextField(
                                value = usernameDraft,
                                onValueChange = { usernameDraft = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                if (usernameDraft.trim().isEmpty()) {
                                                    scope.launch { snackbarHostState.showSnackbar("Username can’t be empty") }
                                                } else {
                                                    viewModel.updateUsername(usernameDraft.trim())
                                                    editingUsername = false
                                                    scope.launch { snackbarHostState.showSnackbar("Username updated") }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Confirm username")
                                        }
                                        IconButton(
                                            onClick = {
                                                editingUsername = false
                                                usernameDraft = ""
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel username edit")
                                        }
                                    }
                                }

                            )
                        } else {
                            ListItem(
                                leadingContent = { Icon(Icons.Default.Person, null) },
                                headlineContent = { Text("Username") },
                                supportingContent = { Text(username ?: "") },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            usernameDraft = username ?: ""
                                            editingUsername = true
                                        }
                                    ) { Icon(Icons.Default.Edit, null) }
                                }
                            )
                        }

                        if (isPasswordUser) {

                            if (editingPwd) {
                                OutlinedTextField(
                                    value = oldPwd,
                                    onValueChange = { oldPwd = it },
                                    label = { Text("Old password") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newPwd,
                                    onValueChange = { newPwd = it },
                                    label = { Text("New password") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            editingPwd = false
                                            oldPwd = ""; newPwd = ""
                                        }
                                    ) { Text("Cancel") }

                                    TextButton(
                                        onClick = {
                                            if (oldPwd.isBlank() || newPwd.isBlank()) {
                                                scope.launch { snackbarHostState.showSnackbar("Fill both password fields") }
                                                return@TextButton
                                            }
                                            val email = user?.email ?: return@TextButton
                                            val cred = EmailAuthProvider.getCredential(email, oldPwd)
                                            user.reauthenticate(cred)
                                                .addOnSuccessListener {
                                                    user.updatePassword(newPwd)
                                                        .addOnSuccessListener {
                                                            editingPwd = false
                                                            oldPwd = ""; newPwd = ""
                                                            scope.launch { snackbarHostState.showSnackbar("Password updated") }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            scope.launch { snackbarHostState.showSnackbar(e.localizedMessage ?: "") }
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    scope.launch { snackbarHostState.showSnackbar(e.localizedMessage ?: "") }
                                                }
                                        }
                                    ) { Text("Save") }
                                }
                            } else {
                                ListItem(
                                    leadingContent = { Icon(Icons.Default.Lock, null) },
                                    headlineContent = { Text("Password") },
                                    supportingContent = { Text("••••••••") },
                                    trailingContent = {
                                        IconButton(onClick = { editingPwd = true }) {
                                            Icon(Icons.Default.Edit, null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = {
                            AuthUI.getInstance().signOut(context)
                            FirebaseAuth.getInstance().signOut()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF95CCCC),
                            contentColor = Color(0xFF003333)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log out")
                    }


                    FilledTonalButton(
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Delete account") }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePwd = ""; deleteError = null
            },
            title = { Text("Delete account?") },
            text = {
                Column {
                    Text("This action is permanent. Enter your password to confirm.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePwd,
                        onValueChange = {
                            deletePwd = it
                            deleteError = null
                        },
                        isError = deleteError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    deleteError?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deletePwd.isBlank()) {
                            deleteError = "Password required"; return@TextButton
                        }
                        viewModel.deleteAccount(deletePwd) { ok, err ->
                            if (ok) {
                                showDeleteDialog = false
                                deletePwd = ""; deleteError = null
                            } else deleteError = err
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deletePwd = ""; deleteError = null
                    }
                ) { Text("Cancel") }
            }
        )
    }
}
