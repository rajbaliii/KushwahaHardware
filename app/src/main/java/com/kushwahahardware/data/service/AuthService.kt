package com.kushwahahardware.data.service

import kotlinx.coroutines.flow.StateFlow

data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String? = null
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object NeedsOTP : AuthResult()
}

interface AuthService {
    val currentUser: StateFlow<AuthUser?>
    
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String, name: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): AuthResult
    suspend fun verifyOTP(email: String, otp: String): AuthResult
    suspend fun signOut()
}
