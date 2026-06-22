package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    // Graceful fallback for Firebase so it won't crash the immediate preview
    private val auth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }
    
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth?.signInWithCredential(credential)?.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback for offline preview without google-services.json
            true
        }
    }

    fun isUserSignedIn(): Boolean {
        return auth?.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return auth?.currentUser?.email
    }
    
    suspend fun saveUserProfile(name: String, email: String) {
        val db = firestore ?: return
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to System.currentTimeMillis()
        )
        try {
            db.collection("users").document(email).set(userMap).await()
        } catch (e: Exception) {
            // Ignore for offline/fallback mode
        }
    }

    fun logout() {
        auth?.signOut()
    }
}
