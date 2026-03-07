package com.kushwahahardware.security

import com.kushwahahardware.data.entity.Permission
import com.kushwahahardware.data.entity.User
import com.kushwahahardware.data.repository.RBACRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    private val repository: RBACRepository
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _currentPermissions = MutableStateFlow<List<Permission>>(emptyList())
    val currentPermissions = _currentPermissions.asStateFlow()

    suspend fun login(user: User) {
        _currentUser.value = user
        if (user.isSuperAdmin) {
            // Super Admin has all permissions conceptually, but we can load all for simplicity
            _currentPermissions.value = repository.getAllPermissions()
        } else {
            user.roleId?.let { roleId ->
                _currentPermissions.value = repository.getPermissionsForRole(roleId)
            } ?: run {
                _currentPermissions.value = emptyList()
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentPermissions.value = emptyList()
    }

    fun canAccess(module: String, action: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.isSuperAdmin) return true
        
        return _currentPermissions.value.any { 
            it.moduleName == module && (it.action == action || it.action == "all") 
        }
    }
}
