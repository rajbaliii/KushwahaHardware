package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Permission
import com.kushwahahardware.data.entity.Role
import com.kushwahahardware.data.repository.RBACRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagePermissionsViewModel @Inject constructor(
    private val repository: RBACRepository
) : ViewModel() {

    private val _roles = repository.getAllRoles()
    val roles: StateFlow<List<Role>> = _roles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRole = MutableStateFlow<Role?>(null)
    val selectedRole = _selectedRole.asStateFlow()

    private val _rolePermissions = MutableStateFlow<List<Permission>>(emptyList())
    val rolePermissions = _rolePermissions.asStateFlow()

    private val _allPermissions = MutableStateFlow<List<Permission>>(emptyList())
    val allPermissions = _allPermissions.asStateFlow()

    init {
        viewModelScope.launch {
            _allPermissions.value = repository.getAllPermissions()
            _roles.first { it.isNotEmpty() }.first().let { 
                selectRole(it)
            }
        }
    }

    fun selectRole(role: Role) {
        _selectedRole.value = role
        viewModelScope.launch {
            _rolePermissions.value = repository.getPermissionsForRole(role.id)
        }
    }

    fun togglePermission(permission: Permission) {
        val current = _rolePermissions.value.toMutableList()
        val existing = current.find { it.id == permission.id }
        if (existing != null) {
            current.remove(existing)
        } else {
            current.add(permission)
        }
        _rolePermissions.value = current
    }

    fun savePermissions() {
        val role = _selectedRole.value ?: return
        viewModelScope.launch {
            repository.updateRolePermissions(role.id, _rolePermissions.value.map { it.id })
        }
    }
}
