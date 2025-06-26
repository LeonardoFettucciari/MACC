package com.example.macc2025.data.repository

import com.example.macc2025.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun addPoints(uid: String, points: Int) = suspendCancellableCoroutine<Unit> { cont ->
        val ref = firestore.collection("users").document(uid)
        ref.update("points", FieldValue.increment(points.toLong()))
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener {
                ref.set(mapOf("points" to points))
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
    }

    override suspend fun getTotalPoints(uid: String): Int = suspendCancellableCoroutine { cont ->
        val ref = firestore.collection("users").document(uid)
        ref.get()
            .addOnSuccessListener { snap ->
                cont.resume(snap.getLong("points")?.toInt() ?: 0)
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
