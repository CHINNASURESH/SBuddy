package com.sbuddy.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import com.sbuddy.app.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db = Firebase.firestore

    // Check if we are running with the mock google-services.json
    val isMockMode: Boolean by lazy {
        try {
            FirebaseApp.getInstance().options.projectId == "mock-project-id"
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private var mockUser: User? = null

        fun createMockSession(user: User) {
            mockUser = user
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(it.uid, it.displayName, it.email)
        } ?: mockUser
    }

    suspend fun login(email: String, password: String): Result<User> {
        if (isMockMode) {
            val user = User(uid = "mock-uid", email = email, displayName = "Mock User")
            mockUser = user
            return Result.success(user)
        }

        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = db.collection("users").document(firebaseUser.uid).get().await()
                val user = document.toObject(User::class.java) ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email)
                Result.success(user)
            } else {
                Result.failure(Exception("User not found after login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<User> {
        if (isMockMode) {
            val user = User(uid = "mock-uid", email = email, displayName = "Mock User")
            mockUser = user
            return Result.success(user)
        }

        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val user = User(uid = firebaseUser.uid, email = email)
                try {
                    db.collection("users").document(user.uid).set(user).await()
                } catch (e: Exception) {
                    // Suppress Firestore write failure as the user is already created in Auth
                }
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        if (isMockMode) {
            val user = User(uid = "mock-uid-google", email = "mock@gmail.com", displayName = "Mock Google User")
            mockUser = user
            return Result.success(user)
        }

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = db.collection("users").document(firebaseUser.uid).get().await()
                // If user doesn't exist in Firestore, create them
                val user = if (document.exists()) {
                    document.toObject(User::class.java) ?: User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email)
                } else {
                    val newUser = User(uid = firebaseUser.uid, email = firebaseUser.email, displayName = firebaseUser.displayName)
                    db.collection("users").document(newUser.uid).set(newUser).await()
                    newUser
                }
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed with Google Sign In"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        if (isMockMode) {
            mockUser = null
        }
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (isMockMode) {
            return Result.success(Unit)
        }
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(user: User): Result<Unit> {
        if (isMockMode) {
            mockUser = user
            return Result.success(Unit)
        }
        return try {
            // Update Firestore
            db.collection("users").document(user.uid).set(user).await()
            // Also update Firebase Auth Profile if needed (displayName)
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(user.displayName)
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
