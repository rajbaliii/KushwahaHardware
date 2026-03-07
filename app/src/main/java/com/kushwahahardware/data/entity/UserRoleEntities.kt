package com.kushwahahardware.data.entity

import androidx.room.*

@Entity(tableName = "roles")
data class Role(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val isSystemRole: Boolean = false
)

@Entity(tableName = "permissions")
data class Permission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleName: String,
    val action: String
)

@Entity(
    tableName = "role_permissions",
    primaryKeys = ["roleId", "permissionId"],
    foreignKeys = [
        ForeignKey(entity = Role::class, parentColumns = ["id"], childColumns = ["roleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Permission::class, parentColumns = ["id"], childColumns = ["permissionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("roleId"),
        Index("permissionId")
    ]
)
data class RolePermission(
    val roleId: Long,
    val permissionId: Long
)

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(entity = Role::class, parentColumns = ["id"], childColumns = ["roleId"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    @ColumnInfo(index = true) val roleId: Long?,
    val phone: String = "",
    val status: String = "ACTIVE", // ACTIVE, INACTIVE
    val isSuperAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.SET_NULL)
    ]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val userId: Long?,
    val userName: String,
    val module: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
