package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.ui.theme.*
import com.kushwahahardware.ui.viewmodel.ExportType
import com.kushwahahardware.ui.viewmodel.SettingsViewModel
import com.kushwahahardware.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shop Info Section
            item {
                SettingsSection(title = "Shop Information") {
                    ShopInfoCard(
                        shopName = uiState.shopInfo.name,
                        location = uiState.shopInfo.location,
                        onEdit = { /* Show edit dialog */ }
                    )
                }
            }
            
            // Export Section
            item {
                SettingsSection(title = "Export Data") {
                    ExportOptions(
                        onExportProducts = { viewModel.exportData(ExportType.PRODUCTS) },
                        onExportSales = { viewModel.exportData(ExportType.SALES) },
                        onExportPurchases = { viewModel.exportData(ExportType.PURCHASES) },
                        onExportStock = { viewModel.exportData(ExportType.STOCK) }
                    )
                }
            }
            
            // Cloud Backup Section
            item {
                SettingsSection(title = "Cloud Backup") {
                    CloudBackupOptions()
                }
            }
            
            // Biometric Lock Section
            item {
                SettingsSection(title = "Security") {
                    BiometricLockOption(
                        enabled = uiState.shopInfo.biometricEnabled,
                        available = uiState.biometricAvailable,
                        onToggle = { viewModel.toggleBiometricLock(it) }
                    )
                }
            }
            
            // Categories Section
            item {
                SettingsSection(title = "Categories") {
                    ManageCategoriesOption(
                        onClick = { navController.navigate(Screen.ManageCategories.route) }
                    )
                }
            }
            
            // App Info
            item {
                SettingsSection(title = "About") {
                    AppInfoCard()
                }
            }
        }
    }
    
    // Show export progress
    if (uiState.exportInProgress) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Exporting…") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun ShopInfoCard(
    shopName: String,
    location: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onEdit),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shopName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Primary
            )
        }
    }
}

@Composable
fun ExportOptions(
    onExportProducts: () -> Unit,
    onExportSales: () -> Unit,
    onExportPurchases: () -> Unit,
    onExportStock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            ExportOptionItem(
                title = "Export Products",
                icon = Icons.Default.Inventory,
                onClick = onExportProducts
            )
            Divider()
            ExportOptionItem(
                title = "Export Sales",
                icon = Icons.Default.TrendingUp,
                onClick = onExportSales
            )
            Divider()
            ExportOptionItem(
                title = "Export Purchases",
                icon = Icons.Default.ShoppingCart,
                onClick = onExportPurchases
            )
            Divider()
            ExportOptionItem(
                title = "Export Stock Report",
                icon = Icons.Default.Assessment,
                onClick = onExportStock
            )
        }
    }
}

@Composable
fun ExportOptionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = "Export",
            tint = TextSecondary
        )
    }
}

@Composable
fun CloudBackupOptions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Implement backup */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Backup to Google Drive",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Implement restore */ }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Restore from Google Drive",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricLockOption(
    enabled: Boolean,
    available: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = if (available) Primary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Biometric Lock",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (available) "Require fingerprint to open app" else "Not available on this device",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = available
            )
        }
    }
}

@Composable
fun ManageCategoriesOption(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Manage Categories",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kushwaha Hardware",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mahanwa, Bihar",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}