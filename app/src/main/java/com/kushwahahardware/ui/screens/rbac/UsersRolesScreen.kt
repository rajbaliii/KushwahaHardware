package com.kushwahahardware.ui.screens.rbac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.UserWithRole
import com.kushwahahardware.ui.viewmodel.UsersRolesViewModel

private val GreenDark = Color(0xFF1B5E20)
private val GreenPrimary = Color(0xFF2E7D32)
private val GreenSurface = Color(0xFFF1F8E9)
private val BluePrimary = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersRolesScreen(
    viewModel: UsersRolesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    onNavigateToEditUser: (Long) -> Unit
) {
    val users by viewModel.filteredUsers.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Admin", "Employee", "Active", "Inactive")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Users & Roles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary)
                            .clickable { onNavigateToAddUser() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add User",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Filter Tab Row ──
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GreenPrimary else Color.White,
                        modifier = Modifier.clickable {
                            selectedFilter = filter
                            viewModel.updateFilter(filter)
                        },
                        shadowElevation = if (isSelected) 0.dp else 1.dp
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── User List ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users) { userWithRole ->
                    UserCard(
                        userWithRole = userWithRole,
                        onClick = { onNavigateToEditUser(userWithRole.user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(userWithRole: UserWithRole, onClick: () -> Unit) {
    val user = userWithRole.user
    val role = userWithRole.role

    val roleName = if (user.isSuperAdmin) "Super Admin" else role?.name ?: "No Role"
    val roleColor = when {
        user.isSuperAdmin -> BluePrimary
        role?.name == "Admin" -> BluePrimary
        else -> GreenPrimary
    }

    val avatarColors = listOf(
        Color(0xFF1976D2), Color(0xFF00897B), Color(0xFFEF6C00),
        Color(0xFF6A1B9A), Color(0xFF2E7D32), Color(0xFFC62828)
    )
    val avatarColor = avatarColors[user.name.hashCode().let { if (it < 0) -it else it } % avatarColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = avatarColor,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
                // Role badge
                Surface(
                    color = roleColor,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = roleName,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // Status badge
                Surface(
                    color = if (user.status == "ACTIVE") GreenPrimary else Color(0xFFD32F2F),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (user.status == "ACTIVE") "Active" else "Inactive",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Chevron for inactive users
                if (user.status != "ACTIVE") {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
