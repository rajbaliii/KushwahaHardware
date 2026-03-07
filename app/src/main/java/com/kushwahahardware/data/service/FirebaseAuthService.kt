package com.kushwahahardware.data.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

@Singleton
class FirebaseAuthService @Inject constructor() : AuthService {
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser

    // Gmail SMTP Configuration
    private val SMTP_HOST = "smtp.gmail.com"
    private val SMTP_PORT = "465"
    private val SENDER_EMAIL = "sendmeok00@gmail.com"
    private val APP_PASSWORD = "ewwo eiog ockz kpmm"

    private val activeOtps = mutableMapOf<String, String>()
    private val pendingRegistrations = mutableMapOf<String, Triple<String, String, String>>() // Email -> (Password, Name, OTP)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user?.let {
                AuthUser(it.uid, it.email ?: "", it.displayName ?: "")
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google Sign-In failed")
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String): AuthResult {
        return try {
            val otp = (100000..999999).random().toString()
            activeOtps[email] = otp
            pendingRegistrations[email] = Triple(password, name, otp)
            
            val success = sendOtpEmail(email, otp)
            if (success) {
                AuthResult.NeedsOTP
            } else {
                AuthResult.Error("Failed to send verification email. Please check your connection.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    private suspend fun sendOtpEmail(recipientEmail: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.socketFactory.port", SMTP_PORT)
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", SMTP_PORT)
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, "Kushwaha Hardware"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Your Verification Code - Kushwaha Hardware"
                setText("""
                    Hello,
                    
                    Thank you for joining Kushwaha Hardware. To complete your registration, please use the following One-Time Password (OTP):
                    
                    OTP: $otp
                    
                    This code is valid for 10 minutes. If you did not request this code, please ignore this email.
                    
                    Best regards,
                    The Kushwaha Hardware Team
                """.trimIndent())
            }
            Transport.send(message)
            Log.d("AUTH_MAIL", "OTP Email sent successfully to $recipientEmail")
            true
        } catch (e: Exception) {
            Log.e("AUTH_MAIL", "Error sending email", e)
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult {
        Log.d("FIREBASE_AUTH", "Attempting to send password reset email to: $email")
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d("FIREBASE_AUTH", "Successfully sent password reset email to: $email")
            AuthResult.Success
        } catch (e: Exception) {
            Log.e("FIREBASE_AUTH", "Failed to send reset email: ${e.message}", e)
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    override suspend fun verifyOTP(email: String, otp: String): AuthResult {
        return if (activeOtps[email] == otp) {
            val pending = pendingRegistrations[email]
            if (pending != null) {
                try {
                    // Create Firebase User now that OTP is verified
                    Log.d("FIREBASE_AUTH", "Attempting to create user with email: $email and password length: ${pending.first.length}")
                    auth.createUserWithEmailAndPassword(email, pending.first).await()
                    
                    // Update display name
                    auth.currentUser?.updateProfile(
                        com.google.firebase.auth.userProfileChangeRequest {
                            displayName = pending.second
                        }
                    )?.await()
                    
                    activeOtps.remove(email)
                    pendingRegistrations.remove(email)
                    AuthResult.Success
                } catch (e: Exception) {
                    AuthResult.Error(e.message ?: "Failed to create account after verification")
                }
            } else {
                // If not a registration (e.g. just email verification later), just succeed
                activeOtps.remove(email)
                AuthResult.Success
            }
        } else {
            AuthResult.Error("Invalid OTP code. Please try again.")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
