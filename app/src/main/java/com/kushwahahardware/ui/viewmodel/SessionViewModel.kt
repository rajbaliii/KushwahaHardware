package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.User
import com.kushwahahardware.data.repository.RBACRepository
import com.kushwahahardware.security.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SessionState {
    object Loading : SessionState()
    object NeedsLogin : SessionState()
    data class NeedsVerification(val email: String, val nextAction: String) : SessionState()
    object NeedsSignUp : SessionState()
    data class NeedsProfileCompletion(val email: String) : SessionState()
    object ForgotPassword : SessionState()
    object ForgotPasswordSuccess : SessionState()
    data class LoggedIn(val user: User) : SessionState()
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: RBACRepository,
    private val authService: com.kushwahahardware.data.service.AuthService,
    val permissionManager: PermissionManager
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            authService.currentUser.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val localUser = repository.getUserByEmail(firebaseUser.email)
                    if (localUser != null) {
                        permissionManager.login(localUser)
                        _sessionState.value = SessionState.LoggedIn(localUser)
                    } else {
                        // User exists in Firebase but not in Local DB, redirect to complete setup
                        _sessionState.value = SessionState.NeedsProfileCompletion(firebaseUser.email)
                    }
                } else {
                    _sessionState.value = SessionState.NeedsLogin
                }
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val result = authService.signInWithEmail(email, pass)
            if (result is com.kushwahahardware.data.service.AuthResult.Error) {
                _loginError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginError.value = null
            val result = authService.signInWithGoogle(idToken)
            if (result is com.kushwahahardware.data.service.AuthResult.Error) {
                _loginError.value = result.message
            }
        }
    }

    fun signUp(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            
            if (email.isBlank() || pass.isBlank() || name.isBlank()) {
                _loginError.value = "All fields are required"
                _isLoading.value = false
                return@launch
            }
            if (pass.length < 6) {
                _loginError.value = "Password must be at least 6 characters long"
                _isLoading.value = false
                return@launch
            }

            val result = authService.signUpWithEmail(email, pass, name)
            when (result) {
                is com.kushwahahardware.data.service.AuthResult.NeedsOTP -> {
                    _sessionState.value = SessionState.NeedsVerification(email, "SIGNUP")
                }
                is com.kushwahahardware.data.service.AuthResult.Error -> {
                    _loginError.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun verifyOtp(email: String, otp: String, nextAction: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val result = authService.verifyOTP(email, otp)
            if (result is com.kushwahahardware.data.service.AuthResult.Success) {
                if (nextAction == "SIGNUP") {
                    _sessionState.value = SessionState.NeedsProfileCompletion(email) 
                }
            } else if (result is com.kushwahahardware.data.service.AuthResult.Error) {
                _loginError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun completeRegistration(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _loginError.value = null
            
            // Define the approved Super Admin emails here
            val superAdminEmails = listOf("rajbalikumar697@gmail.com")
            val isSuperAdmin = superAdminEmails.contains(email)
            
            val newUser = User(
                name = name,
                email = email,
                phone = phone,
                passwordHash = "FIREBASE_AUTH",
                roleId = if (isSuperAdmin) null else 1L, // Basic employee role by default, null means Super Admin if isSuperAdmin=true
                isSuperAdmin = isSuperAdmin,
                status = "ACTIVE"
            )
            val userId = repository.insertUser(newUser)
            val user = repository.getUserById(userId)
            if (user != null) {
                permissionManager.login(user)
                _sessionState.value = SessionState.LoggedIn(user)
            }
        }
    }

    fun startForgotPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val result = authService.sendPasswordResetEmail(email)
            if (result is com.kushwahahardware.data.service.AuthResult.Success) {
                _sessionState.value = SessionState.ForgotPasswordSuccess
            } else if (result is com.kushwahahardware.data.service.AuthResult.Error) {
                _loginError.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun setSessionState(state: SessionState) {
        _sessionState.value = state
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
            permissionManager.logout()
            _sessionState.value = SessionState.NeedsLogin
        }
    }
}
