package com.sbuddy.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.sbuddy.app.data.model.User

class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(it.uid, it.displayName, it.email)
        }
    }

    // Placeholder for login/signup methods which would be implemented here
    // In a real app, these would use suspend functions and return Result<User>
}
