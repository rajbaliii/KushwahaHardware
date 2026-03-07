package com.kushwahahardware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.R
import com.kushwahahardware.ui.components.QuickActionButton
import com.kushwahahardware.ui.components.ShopHeader
import com.kushwahahardware.ui.components.SummaryCard
import com.kushwahahardware.ui.components.SalesChart
import com.kushwahahardware.ui.viewmodel.DashboardViewModel

import com.kushwahahardware.ui.theme.DesignTokens
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToNewSale: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToNewPurchase: () -> Unit, // Renamed for consistency
    onNavigateToPendingDues: () -> Unit,
    onNavigateToInventory: (Boolean) -> Unit
) {

    val totalProducts by viewModel.totalProducts.collectAsState()
    val todaySales by viewModel.todaySales.collectAsState()
    val todayProfit by viewModel.todayProfit.collectAsState()
    val grossMargin by viewModel.grossMargin.collectAsState()
    val pendingAmount by viewModel.pendingAmount.collectAsState()
    val lowStockCount by viewModel.lowStockCount.collectAsState()
    val weeklySales by viewModel.weeklySales.collectAsState()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ShopHeader()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelLarge,
                        color = DesignTokens.SubtitleGray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Business Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Summary Grid 2x2
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryCard(
                            title = stringResource(R.string.total_products),
                            value = totalProducts.toString(),
                            icon = Icons.Default.Inventory2,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)) {
                            SummaryCard(
                                title = stringResource(R.string.today_sales),
                                value = "₹${String.format("%.0f", todaySales)}",
                                icon = Icons.Default.TrendingUp,
                                containerColor = Color(0xFFF1F8E9), // Soft Success Green
                                iconTint = DesignTokens.SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            SummaryCard(
                                title = "Sales",
                                value = "---",
                                icon = Icons.Default.Lock,
                                containerColor = Color.LightGray.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)) {
                            SummaryCard(
                                title = stringResource(R.string.pending_amount),
                                value = "₹${String.format("%.0f", pendingAmount)}",
                                icon = Icons.Default.AccountBalanceWallet,
                                containerColor = Color(0xFFFFF7E6), // Soft Warning Gold
                                iconTint = DesignTokens.GoldAccent,
                                modifier = Modifier.weight(1f).clickable { onNavigateToPendingDues() }
                            )
                        } else {
                            SummaryCard(
                                title = "Pending",
                                value = "---",
                                icon = Icons.Default.Lock,
                                containerColor = Color.LightGray.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        val lowStockColor = if (lowStockCount > 0) Color(0xFFFFEBEE) else Color.White
                        val lowStockTint = if (lowStockCount > 0) DesignTokens.ErrorRed else DesignTokens.MidTeal
                        SummaryCard(
                            title = stringResource(R.string.low_stock),
                            value = lowStockCount.toString(),
                            icon = Icons.Default.Warning,
                            containerColor = lowStockColor,
                            iconTint = lowStockTint,
                            badge = if (lowStockCount > 0) "ALERT" else null,
                            modifier = Modifier.weight(1f).clickable { 
                                onNavigateToInventory(true)
                            }
                        )
                    }

                    if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryCard(
                                title = "Profit Today",
                                value = "₹${String.format("%.0f", todayProfit)}",
                                icon = Icons.Default.Savings,
                                containerColor = Color(0xFFE1F5FE), // Soft Blue
                                iconTint = Color(0xFF0288D1),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Gross Margin",
                                value = "${String.format("%.1f", grossMargin)}%",
                                icon = Icons.Default.AccountBalance,
                                containerColor = Color(0xFFF3E5F5), // Soft Purple
                                iconTint = Color(0xFF7B1FA2),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Quick Actions Grid 2x2
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.SALES, com.kushwahahardware.security.AppActions.ADD)) {
                            QuickActionButton(
                                text = stringResource(R.string.new_sale),
                                icon = Icons.Default.ShoppingBag,
                                onClick = onNavigateToNewSale,
                                containerColor = DesignTokens.MidTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.INVENTORY, com.kushwahahardware.security.AppActions.ADD)) {
                            QuickActionButton(
                                text = "Add Item",
                                icon = Icons.Default.AddBox,
                                onClick = onNavigateToAddProduct,
                                containerColor = DesignTokens.PremiumTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.PURCHASE, com.kushwahahardware.security.AppActions.ADD)) {
                            QuickActionButton(
                                text = "Purchase",
                                icon = Icons.Default.ReceiptLong,
                                onClick = onNavigateToNewPurchase,
                                containerColor = DesignTokens.GoldAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        QuickActionButton(
                            text = "Insights",
                            icon = Icons.Default.PieChart,
                            onClick = { /* Navigate to reports */ },
                            containerColor = DesignTokens.TealAccent,
                            contentColor = DesignTokens.DeepTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = DesignTokens.TealAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = DesignTokens.SuccessGreen, modifier = Modifier.size(16.dp))
                            Text(" +12%", color = DesignTokens.SuccessGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }

                if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f))
                    ) {
                        SalesChart(
                            salesData = weeklySales,
                            modifier = Modifier.padding(24.dp).fillMaxWidth().height(220.dp)
                        )
                    }
                }
            }

            if (lowStockCount > 0) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    AlertCard(count = lowStockCount)
                }
            }
        }
    }
}

@Composable
fun AlertCard(count: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Low Stock Alert",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$count products are below minimum stock level.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
