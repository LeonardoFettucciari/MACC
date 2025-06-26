package com.example.macc2025.domain.repository

interface UserRepository {
    suspend fun addPoints(uid: String, points: Int)
    suspend fun getTotalPoints(uid: String): Int
}
