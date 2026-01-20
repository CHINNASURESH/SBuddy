package com.sbuddy.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sbuddy.app.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(it.uid, it.displayName, it.email)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = document.toObject(User::class.java) ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email)
                Result.success(user)
            } else {
                Result.failure(Exception("User not found after login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, mobile: String): Result<User> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val user = User(uid = firebaseUser.uid, email = email, mobile = mobile)
                firestore.collection("users").document(user.uid).set(user).await()
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
