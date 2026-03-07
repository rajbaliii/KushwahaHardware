package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RBACRepository @Inject constructor(
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val activityLogDao: ActivityLogDao
) {
    // Users
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Roles & Permissions
    fun getAllRoles(): Flow<List<Role>> = roleDao.getAllRoles()
    suspend fun insertRole(role: Role): Long = roleDao.insertRole(role)
    suspend fun getPermissionsForRole(roleId: Long): List<Permission> = roleDao.getPermissionsForRole(roleId)
    suspend fun getAllPermissions(): List<Permission> = roleDao.getAllPermissions()
    suspend fun insertPermission(permission: Permission) = roleDao.insertPermission(permission)
    
    suspend fun updateRolePermissions(roleId: Long, permissionIds: List<Long>) {
        roleDao.clearPermissionsForRole(roleId)
        permissionIds.forEach { permissionId ->
            roleDao.insertRolePermission(RolePermission(roleId, permissionId))
        }
    }

    // Activity Logs
    fun getAllLogs(): Flow<List<ActivityLog>> = activityLogDao.getAllLogs()
    suspend fun logAction(userId: Long?, userName: String, module: String, action: String, details: String) {
        activityLogDao.insertLog(ActivityLog(userId = userId, userName = userName, module = module, action = action, details = details))
    }
}
