package com.kushwahahardware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.SettingsViewModel
import com.kushwahahardware.security.rememberCanAccess
import com.kushwahahardware.security.rememberIsSuperAdmin
import com.kushwahahardware.security.AppModules
import com.kushwahahardware.security.AppActions

private val SettingsGreen = Color(0xFF2E7D32)
private val SettingsGreenDark = Color(0xFF004D40)
private val SettingsBg = Color(0xFFF7F7F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToUsersRoles: () -> Unit = {},
    onNavigateToActivityLogs: () -> Unit = {},
    onNavigateToManagePermissions: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val shopInfo by viewModel.shopInfo.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showAppPrefsDialog by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopPhone by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var bankingName by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }

    LaunchedEffect(shopInfo) {
        shopInfo?.let {
            shopName = it.shopName
            shopAddress = it.address
            shopPhone = it.phone ?: ""
            ownerName = it.ownerName
            altPhone = it.alternativePhone ?: ""
            bankingName = it.bankingName
            upiId = it.upiId
            gstNumber = it.gstNumber ?: ""
        }
    }

    val canViewUsers = rememberCanAccess(AppModules.USERS_ROLES, AppActions.VIEW)
    val canViewLogs = rememberCanAccess(AppModules.ACTIVITY_LOGS, AppActions.VIEW)
    val isSuperAdmin = rememberIsSuperAdmin()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SettingsBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ──── Main Settings Card ────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column {
                    SettingsListItem(
                        icon = Icons.Default.Business,
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1976D2),
                        title = "Business Settings",
                        subtitle = "Shop info, address & banking",
                        onClick = { 
                            coroutineScope.launch { 
                                scrollState.animateScrollTo(scrollState.maxValue) 
                            } 
                        }
                    )

                    if (canViewUsers) {
                        SettingsDivider()
                        SettingsListItem(
                            icon = Icons.Default.Group,
                            iconBg = Color(0xFFE8F5E9),
                            iconTint = SettingsGreen,
                            title = "Users & Roles",
                            subtitle = "Manage staff & permissions",
                            onClick = onNavigateToUsersRoles
                        )
                    }

                    if (canViewLogs) {
                        SettingsDivider()
                        SettingsListItem(
                            icon = Icons.Default.History,
                            iconBg = Color(0xFFFFF3E0),
                            iconTint = Color(0xFFE65100),
                            title = "Activity Logs",
                            subtitle = "View all recent actions",
                            onClick = onNavigateToActivityLogs
                        )
                    }

                    if (isSuperAdmin) {
                        SettingsDivider()
                        SettingsListItem(
                            icon = Icons.Default.Security,
                            iconBg = Color(0xFFF3E5F5),
                            iconTint = Color(0xFF7B1FA2),
                            title = "Backup & Restore",
                            subtitle = "Cloud sync & local backup",
                            onClick = { showBackupDialog = true }
                        )
                    }

                    SettingsDivider()
                    SettingsListItem(
                        icon = Icons.Default.AppSettingsAlt,
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF0277BD),
                        title = "App Preferences",
                        subtitle = "Notifications, display & more",
                        onClick = { showAppPrefsDialog = true }
                    )

                    SettingsDivider()
                    SettingsListItem(
                        icon = Icons.Default.Logout,
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFC62828),
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = onLogout,
                        titleColor = Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ──── Business Info Section ────
            Text(
                "BUSINESS INFO",
                style = MaterialTheme.typography.labelLarge,
                color = SettingsGreenDark,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = shopName, onValueChange = { shopName = it },
                        label = { Text("Shop Name") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = ownerName, onValueChange = { ownerName = it },
                        label = { Text("Owner Name") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = shopAddress, onValueChange = { shopAddress = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = shopPhone, onValueChange = { shopPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    Text(
                        "Payment & Tax",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SettingsGreenDark
                    )
                    OutlinedTextField(
                        value = gstNumber, onValueChange = { gstNumber = it },
                        label = { Text("GST Number") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = bankingName, onValueChange = { bankingName = it },
                        label = { Text("Banking Name") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = upiId, onValueChange = { upiId = it },
                        label = { Text("UPI ID") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.updateShopInfo(
                                shopName, shopAddress, shopPhone, ownerName,
                                altPhone, bankingName, upiId, gstNumber
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = shopName.isNotBlank() && upiId.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SettingsGreen)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE CHANGES", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Backup & Restore Dialog
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Backup & Restore") },
                text = { Text("Cloud synchronization and local database backup features are coming soon. Your data is currently stored safely on your device.") },
                confirmButton = {
                    Button(onClick = { showBackupDialog = false }) { Text("OK") }
                }
            )
        }

        // App Preferences Dialog
        if (showAppPrefsDialog) {
            AlertDialog(
                onDismissRequest = { showAppPrefsDialog = false },
                title = { Text("App Preferences") },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dark Mode (Experimental)")
                            Switch(checked = darkModeEnabled, onCheckedChange = { darkModeEnabled = it })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fast Invoice Generation")
                            Switch(checked = true, onCheckedChange = { })
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAppPrefsDialog = false }) { Text("Done") }
                }
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = Color(0xFFF2F2F2)
    )
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(20.dp)
        )
    }
}

// backward compatibility
@Composable
fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    SettingsListItem(
        icon = icon,
        iconBg = iconTint.copy(alpha = 0.1f),
        iconTint = iconTint,
        title = title,
        subtitle = "",
        onClick = onClick
    )
    if (showDivider) SettingsDivider()
}
