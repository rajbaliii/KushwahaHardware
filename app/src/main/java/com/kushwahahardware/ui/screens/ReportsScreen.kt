package com.kushwahahardware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kushwahahardware.ui.theme.*
import com.kushwahahardware.ui.viewmodel.ReportsViewModel
import com.kushwahahardware.utils.CurrencyUtils
import com.kushwahahardware.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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
                .padding(padding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Summary
            item {
                TodaySummaryCard(uiState)
            }
            
            // Report Types
            item {
                Text(
                    text = "Generate Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                ReportTypeCard(
                    title = "Sales Report",
                    description = "View sales by date range",
                    icon = Icons.Default.TrendingUp,
                    color = Success,
                    onClick = { /* Show date picker and generate */ }
                )
            }
            
            item {
                ReportTypeCard(
                    title = "Profit Report",
                    description = "View profit by date range",
                    icon = Icons.Default.AttachMoney,
                    color = Accent,
                    onClick = { /* Show date picker and generate */ }
                )
            }
            
            item {
                ReportTypeCard(
                    title = "Stock Report",
                    description = "View low stock and inventory",
                    icon = Icons.Default.Inventory,
                    color = Primary,
                    onClick = { viewModel.generateStockReport() }
                )
            }
            
            item {
                ReportTypeCard(
                    title = "Supplier Pending",
                    description = "View pending payments to suppliers",
                    icon = Icons.Default.AccountBalance,
                    color = Warning,
                    onClick = { viewModel.generateSupplierPendingReport() }
                )
            }
            
            item {
                ReportTypeCard(
                    title = "Customer Pending",
                    description = "View pending payments from customers",
                    icon = Icons.Default.People,
                    color = Error,
                    onClick = { viewModel.generateCustomerPendingReport() }
                )
            }
            
            // Low Stock Alert
            item {
                LowStockReportSection(uiState)
            }
        }
    }
}

@Composable
fun TodaySummaryCard(uiState: com.kushwahahardware.ui.viewmodel.ReportsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Sales",
                    value = CurrencyUtils.formatWithSymbol(uiState.todaySales),
                    color = Success
                )
                SummaryItem(
                    label = "Purchases",
                    value = CurrencyUtils.formatWithSymbol(uiState.todayPurchases),
                    color = Primary
                )
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ReportTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun LowStockReportSection(uiState: com.kushwahahardware.ui.viewmodel.ReportsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Low Stock Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.lowStockProducts.isNotEmpty()) {
                    Badge { Text(uiState.lowStockProducts.size.toString()) }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.lowStockProducts.isEmpty()) {
                Text(
                    text = "No low stock items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                uiState.lowStockProducts.take(5).forEach { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${product.currentStock}/${product.lowStockAlert}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}