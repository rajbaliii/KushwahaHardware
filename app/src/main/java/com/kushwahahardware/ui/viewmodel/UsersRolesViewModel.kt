package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Role
import com.kushwahahardware.data.entity.User
import com.kushwahahardware.data.repository.RBACRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersRolesViewModel @Inject constructor(
    private val repository: RBACRepository
) : ViewModel() {

    private val _users = repository.getAllUsers()
    private val _roles = repository.getAllRoles()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("All") // All, Admin, Employee, Active, Inactive

    val roles: StateFlow<List<Role>> = _roles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredUsers: StateFlow<List<UserWithRole>> = combine(
        _users,
        _roles,
        _searchQuery,
        _selectedFilter
    ) { users, roles, query, filter ->
        users.map { user ->
            UserWithRole(user, roles.find { it.id == user.roleId })
        }.filter {
            val matchesSearch = it.user.name.contains(query, ignoreCase = true) || it.user.email.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Admin" -> it.role?.name == "Admin"
                "Employee" -> it.role?.name == "Employee"
                "Active" -> it.user.status == "ACTIVE"
                "Inactive" -> it.user.status == "INACTIVE"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        _selectedFilter.value = filter
    }
}

data class UserWithRole(
    val user: User,
    val role: Role?
)
