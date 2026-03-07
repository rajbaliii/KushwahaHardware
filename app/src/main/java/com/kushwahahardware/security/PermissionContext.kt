package com.kushwahahardware.security

import androidx.compose.runtime.*

/**
 * CompositionLocal that provides PermissionManager to any composable in the tree.
 * Provides a no-op default so previews don't crash.
 */
val LocalPermissionManager = staticCompositionLocalOf<PermissionManager?> { null }

/**
 * Convenient composable helper to check a permission from anywhere in the Compose tree.
 * Returns true if the current user can perform [action] on [module].
 */
@Composable
fun rememberCanAccess(module: String, action: String): Boolean {
    val pm = LocalPermissionManager.current ?: return false
    val permissions by pm.currentPermissions.collectAsState()
    val user by pm.currentUser.collectAsState()
    
    // Trigger recomposition when user or permissions change
    return remember(user, permissions) {
        pm.canAccess(module, action)
    }
}

/**
 * Returns true if the current user is a Super Admin.
 */
@Composable
fun rememberIsSuperAdmin(): Boolean {
    val pm = LocalPermissionManager.current ?: return false
    val user by pm.currentUser.collectAsState()
    return user?.isSuperAdmin == true
}
