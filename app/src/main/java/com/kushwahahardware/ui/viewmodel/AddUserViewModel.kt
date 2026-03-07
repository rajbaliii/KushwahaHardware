package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Role
import com.kushwahahardware.data.entity.User
import com.kushwahahardware.data.repository.RBACRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.inject.Inject

@HiltViewModel
class AddUserViewModel @Inject constructor(
    private val repository: RBACRepository,
    private val permissionManager: com.kushwahahardware.security.PermissionManager
) : ViewModel() {

    private val _roles = repository.getAllRoles()
    val roles: StateFlow<List<Role>> = _roles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        roleId: Long,
        isSuperAdmin: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // In a real app, we'd hash the password here. For this demo, we'll store as is.
            repository.insertUser(
                User(
                    name = name,
                    email = email,
                    passwordHash = password,
                    roleId = if (isSuperAdmin) null else roleId,
                    phone = phone,
                    isSuperAdmin = isSuperAdmin
                )
            )
            
            val currentUser = permissionManager.currentUser.value
            repository.logAction(
                userId = currentUser?.id,
                userName = currentUser?.name ?: "System",
                module = com.kushwahahardware.security.AppModules.USERS_ROLES,
                action = com.kushwahahardware.security.AppActions.ADD,
                details = "Created user: $name ($phone)"
            )
            
            onSuccess()
        }
    }

    fun generatePassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val random = SecureRandom()
        return (1..12).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }
}
