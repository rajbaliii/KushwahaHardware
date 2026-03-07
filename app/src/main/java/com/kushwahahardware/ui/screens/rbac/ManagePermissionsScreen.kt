package com.kushwahahardware.ui.screens.rbac

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Permission
import com.kushwahahardware.data.entity.Role
import com.kushwahahardware.ui.viewmodel.ManagePermissionsViewModel

private val GreenDeep = Color(0xFF004D40)
private val GreenMain = Color(0xFF2E7D32)
private val GreenChip = Color(0xFF1B5E20)

// Map module names to Material icons
private fun moduleIcon(moduleName: String): ImageVector = when (moduleName.lowercase()) {
    "dashboard" -> Icons.Default.Dashboard
    "inventory" -> Icons.Default.Inventory
    "sales" -> Icons.Default.ShoppingCart
    "purchase" -> Icons.Default.ShoppingBag
    "reports" -> Icons.Default.BarChart
    "users_roles", "users" -> Icons.Default.Group
    "activity_logs" -> Icons.Default.History
    else -> Icons.Default.Settings
}

private fun moduleDisplayName(moduleName: String): String = when (moduleName.lowercase()) {
    "users_roles" -> "Users & Roles"
    "activity_logs" -> "Activity Logs"
    else -> moduleName.replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePermissionsScreen(
    viewModel: ManagePermissionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val roles by viewModel.roles.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val rolePermissions by viewModel.rolePermissions.collectAsState()
    val allPermissions by viewModel.allPermissions.collectAsState()

    val modules = allPermissions.map { it.moduleName }.distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Role Permissions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.savePermissions() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenMain)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Role Tab Row ──
            Surface(color = Color.White, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roles.forEach { role ->
                        val isSelected = selectedRole?.id == role.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) GreenMain else Color(0xFFF0F0F0),
                            modifier = Modifier.clickable { viewModel.selectRole(role) }
                        ) {
                            Text(
                                text = role.name,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Permission Modules ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(modules) { moduleName ->
                    val modulePerms = allPermissions.filter { it.moduleName == moduleName }
                    val selectedPerms = rolePermissions.filter { it.moduleName == moduleName }
                    ModulePermissionCard2(
                        moduleName = moduleName,
                        allPermissions = modulePerms,
                        selectedPermissions = selectedPerms,
                        onToggle = { viewModel.togglePermission(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModulePermissionCard2(
    moduleName: String,
    allPermissions: List<Permission>,
    selectedPermissions: List<Permission>,
    onToggle: (Permission) -> Unit
) {
    val isModuleEnabled = selectedPermissions.isNotEmpty()
    val displayName = moduleDisplayName(moduleName)
    val icon = moduleIcon(moduleName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Module header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Module icon container
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GreenMain.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GreenMain,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isModuleEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            allPermissions.filter { p -> selectedPermissions.none { it.id == p.id } }
                                .forEach { onToggle(it) }
                        } else {
                            selectedPermissions.forEach { onToggle(it) }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GreenMain,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }

            AnimatedVisibility(visible = isModuleEnabled) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFF0F0F0)
                    )

                    // Action rows – View / Add / Edit / Delete
                    val actionOrder = listOf("view", "add", "edit", "delete")
                    val sortedPerms = allPermissions.sortedBy { p ->
                        actionOrder.indexOf(p.action.lowercase()).let { if (it == -1) 99 else it }
                    }

                    sortedPerms.forEach { permission ->
                        val isSelected = selectedPermissions.any { it.id == permission.id }
                        PermissionRow(
                            action = permission.action,
                            isSelected = isSelected,
                            onToggle = { onToggle(permission) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(action: String, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // A small numbered badge for the order
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(GreenMain.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (action.lowercase()) {
                    "view" -> "V"
                    "add" -> "A"
                    "edit" -> "E"
                    "delete" -> "D"
                    else -> action.take(1).uppercase()
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GreenMain
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = action.replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            color = Color(0xFF424242),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Checkbox row on right side
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Primary checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isSelected) GreenMain else Color(0xFFE0E0E0))
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// Backward compat
@Composable
fun ModulePermissionCard(
    moduleName: String,
    allPermissions: List<Permission>,
    selectedPermissions: List<Permission>,
    onToggle: (Permission) -> Unit
) = ModulePermissionCard2(moduleName, allPermissions, selectedPermissions, onToggle)
