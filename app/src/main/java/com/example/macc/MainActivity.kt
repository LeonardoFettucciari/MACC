package com.example.macc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.macc.navigation.AppNavHost
import com.example.macc.presentation.ui.LoginScreen
import com.example.macc.presentation.ui.theme.MACC2025Theme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val auth = remember { FirebaseAuth.getInstance() }
            var user by remember { mutableStateOf(auth.currentUser) }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    user = firebaseAuth.currentUser
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            MACC2025Theme {
                if (user != null) {
                    AppNavHost()
                } else {
                    LoginScreen()
                }
            }
        }
    }
}
