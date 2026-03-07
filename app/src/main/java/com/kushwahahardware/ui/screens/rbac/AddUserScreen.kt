package com.kushwahahardware.ui.screens.rbac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.AddUserViewModel

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenDark2 = Color(0xFF1B5E20)
private val GreenLight2 = Color(0xFFF1F8E9)
private val BluePrimary2 = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    viewModel: AddUserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedRoleId by remember { mutableStateOf<Long?>(null) }
    var isSuperAdmin by remember { mutableStateOf(false) }

    val roles by viewModel.roles.collectAsState()

    LaunchedEffect(roles) {
        if (selectedRoleId == null && roles.isNotEmpty()) {
            selectedRoleId = roles.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add User", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Info Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Personal Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF004D40),
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "+91",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                VerticalDivider(
                                    modifier = Modifier.height(20.dp),
                                    color = Color(0xFFE0E0E0)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Email", color = Color.LightGray) }
                    )

                    // Password row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.weight(1f),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.clickable {
                                password = viewModel.generatePassword()
                                showPassword = true
                            }
                        ) {
                            Text(
                                "Generate",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Assign Role Section ──
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Assign Role",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF004D40),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Super Admin card
                RoleCard2(
                    title = "Super Admin",
                    description = "Full system access",
                    icon = Icons.Default.Shield,
                    tint = BluePrimary2,
                    selected = isSuperAdmin,
                    onClick = { isSuperAdmin = true; selectedRoleId = null }
                )

                roles.forEach { role ->
                    val (icon2, tint2) = when (role.name) {
                        "Admin" -> Icons.Default.AdminPanelSettings to BluePrimary2
                        "Employee" -> Icons.Default.Person to GreenPrimary
                        else -> Icons.Default.Person to Color(0xFF9E9E9E)
                    }
                    RoleCard2(
                        title = role.name,
                        description = role.description,
                        icon = icon2,
                        tint = tint2,
                        selected = !isSuperAdmin && selectedRoleId == role.id,
                        onClick = { isSuperAdmin = false; selectedRoleId = role.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Save Button ──
            Button(
                onClick = {
                    val rId = if (isSuperAdmin) 0L else selectedRoleId ?: 0L
                    viewModel.saveUser(name, email, phone, password, rId, isSuperAdmin) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = name.isNotBlank() && password.isNotBlank()
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleCard2(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) tint.copy(alpha = 0.07f) else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) tint else Color(0xFFE8E8E8)
        ),
        elevation = CardDefaults.cardElevation(if (selected) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox on left
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) tint else Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Icon in a colored circle
            Surface(
                color = tint.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (selected) tint else Color.Black
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (selected) tint.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}

// Kept for backward compatibility – delegates to RoleCard2
@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit
) = RoleCard2(title, description, icon, tint, selected, onClick)
