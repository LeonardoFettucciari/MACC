package com.example.macc2025.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc2025.domain.repository.UserRepository
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
            } catch (_: Exception) { }
        }
    }

    fun updateUsername(name: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.setUsername(uid, name)
                _username.value = name
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
}
