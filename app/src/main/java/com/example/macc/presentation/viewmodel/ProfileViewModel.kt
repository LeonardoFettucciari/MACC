package com.example.macc.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc.domain.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _usernameLoaded = MutableStateFlow(false)
    val usernameLoaded: StateFlow<Boolean> = _usernameLoaded

    private val _ranking = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val ranking: StateFlow<List<Pair<String, Int>>> = _ranking

    init {
        loadPoints()
        loadUsername()
    }

    fun loadPoints() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _points.value = userRepository.getTotalPoints(uid)
            } catch (_: Exception) { }
        }
    }

    fun loadUsername() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _username.value = userRepository.getUsername(uid)
            } catch (_: Exception) {
            } finally {
                _usernameLoaded.value = true
            }
        }
    }

    fun updateUsername(name: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.setUsername(uid, name)
                _username.value = name
                _usernameLoaded.value = true
            } catch (_: Exception) { }
        }
    }

    fun loadRanking() {
        viewModelScope.launch {
            try {
                _ranking.value = userRepository.getTopUsers(5)
            } catch (_: Exception) { }
        }
    }

    fun deleteAccount(
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return onResult(false, "No user")

        val email = user.email
            ?: return onResult(false, "Email not found")

        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {

                user.delete()
                    .addOnSuccessListener {

                        viewModelScope.launch {
                            try { userRepository.deleteUser(user.uid) } catch (_: Exception) { }
                            onResult(true, null)
                        }
                    }
                    .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }


}
