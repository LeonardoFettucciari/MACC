package com.example.macc.presentation.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macc.R
import com.google.firebase.auth.FirebaseAuth
import com.example.macc.presentation.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val auth = remember { FirebaseAuth.getInstance() }

    val launcher = rememberLauncherForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        if (res.resultCode != Activity.RESULT_OK) {
            errorMessage = res.idpResponse?.error?.localizedMessage
        }
    }

    val googleSignIn = {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        launcher.launch(intent)
    }

    val emailSignIn: () -> Unit = {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnFailureListener { errorMessage = it.localizedMessage }
    }

    val emailSignUp: () -> Unit = {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnFailureListener { errorMessage = it.localizedMessage }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Sign In") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.text_logo_large),
                contentDescription = "App logo",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Teal,
                    focusedLabelColor = Teal,
                    cursorColor = Teal
                )

            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Teal,
                    focusedLabelColor = Teal,
                    cursorColor = Teal
                )
            )
            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = emailSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF31514f),
                        contentColor   = Color.White
                    )
                ) {
                    Text("Sign In")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = emailSignUp,
                    border = BorderStroke(1.dp, Color(0xFF31514f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF31514f)
                    )
                ) {
                    Text("Sign Up")
                }
            }

            Spacer(Modifier.height(24.dp))

            GoogleSignInButton(onClick = googleSignIn)

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Composable
fun GoogleSignInButton(
    text: String = "Continue with Google",
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .width(240.dp)
            .height(48.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.google_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Color(0xFF1F1F1F),
            fontSize = 14.sp
        )
    }
}
