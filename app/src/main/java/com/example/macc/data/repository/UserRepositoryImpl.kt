package com.example.macc.data.repository

import com.example.macc.domain.repository.UserRepository
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

    override suspend fun getUsername(uid: String): String? = suspendCancellableCoroutine { cont ->
        val ref = firestore.collection("users").document(uid)
        ref.get()
            .addOnSuccessListener { snap -> cont.resume(snap.getString("username")) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun setUsername(uid: String, username: String) = suspendCancellableCoroutine<Unit> { cont ->
        val ref = firestore.collection("users").document(uid)
        ref.set(mapOf("username" to username), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun getTopUsers(limit: Int): List<Pair<String, Int>> = suspendCancellableCoroutine { cont ->
        firestore.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { d ->
                    val username = d.getString("username") ?: return@mapNotNull null
                    val pts = d.getLong("points")?.toInt() ?: 0
                    username to pts
                }
                cont.resume(list)
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
