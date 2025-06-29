package com.example.macc.domain.repository

interface UserRepository {
    suspend fun addPoints(uid: String, points: Int)
    suspend fun getTotalPoints(uid: String): Int
    suspend fun getUsername(uid: String): String?
    suspend fun setUsername(uid: String, username: String)
    suspend fun getTopUsers(limit: Int): List<Pair<String, Int>>
    suspend fun deleteUser(uid: String)

}
