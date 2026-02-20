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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.ui.theme.*
import com.kushwahahardware.ui.viewmodel.DashboardViewModel
import com.kushwahahardware.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Kushwaha Hardware",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Mahanwa, Bihar",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
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
            // Summary Cards
            item {
                SummaryCardsRow(uiState.summary)
            }
            
            // Quick Actions
            item {
                QuickActionsSection(navController)
            }
            
            // Low Stock Alerts
            item {
                LowStockAlertsSection(
                    lowStockProducts = uiState.summary.lowStockProducts,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SummaryCardsRow(summary: com.kushwahahardware.data.repository.DashboardSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Total Products",
                value = summary.totalProducts.toString(),
                icon = Icons.Default.Inventory,
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Today's Sales",
                value = CurrencyUtils.formatWithSymbol(summary.todaySales),
                icon = Icons.Default.TrendingUp,
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }
        
        SummaryCard(
            title = "Total Pending",
            value = CurrencyUtils.formatWithSymbol(summary.totalPending),
            icon = Icons.Default.Warning,
            color = Warning,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    text = "New Sale",
                    icon = Icons.Default.PointOfSale,
                    color = Success,
                    onClick = { navController.navigate(Screen.CreateSale.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Add Purchase",
                    icon = Icons.Default.ShoppingCart,
                    color = Primary,
                    onClick = { navController.navigate(Screen.AddPurchase.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            QuickActionButton(
                text = "Add Product",
                icon = Icons.Default.AddBox,
                color = Accent,
                onClick = { navController.navigate(Screen.AddProduct.createRoute()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LowStockAlertsSection(
    lowStockProducts: List<Product>,
    navController: NavController
) {
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
                    text = "Low Stock Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (lowStockProducts.isNotEmpty()) Error else TextPrimary
                )
                if (lowStockProducts.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            Badge { Text(lowStockProducts.size.toString()) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Stock",
                            tint = Error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (lowStockProducts.isEmpty()) {
                Text(
                    text = "No low stock items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                lowStockProducts.take(5).forEach { product ->
                    LowStockItem(product = product)
                    if (product != lowStockProducts.take(5).last()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                
                if (lowStockProducts.size > 5) {
                    TextButton(
                        onClick = { navController.navigate(Screen.Inventory.route) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("View All (${lowStockProducts.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockItem(product: Product) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Brand: ${product.brand}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${product.currentStock} left",
                style = MaterialTheme.typography.bodyMedium,
                color = Error,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Alert: ${product.lowStockAlert}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}