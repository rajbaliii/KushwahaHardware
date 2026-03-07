package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface RoleDao {
    @Query("SELECT * FROM roles ORDER BY name ASC")
    fun getAllRoles(): Flow<List<Role>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: Role): Long

    @Query("SELECT * FROM permissions")
    suspend fun getAllPermissions(): List<Permission>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPermission(permission: Permission): Long

    @Query("SELECT p.* FROM permissions p " +
           "JOIN role_permissions rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId = :roleId")
    suspend fun getPermissionsForRole(roleId: Long): List<Permission>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRolePermission(rolePermission: RolePermission)

    @Query("DELETE FROM role_permissions WHERE roleId = :roleId")
    suspend fun clearPermissionsForRole(roleId: Long)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Insert
    suspend fun insertLog(log: ActivityLog)
}
