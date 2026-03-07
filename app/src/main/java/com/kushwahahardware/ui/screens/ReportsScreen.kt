package com.kushwahahardware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.ReportsViewModel
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val totalSales by viewModel.totalSalesAmount.collectAsState()
    val lowStockCount by viewModel.lowStockItems.collectAsState()

    val canView = com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            ) 
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        if (!canView) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(androidx.compose.material.icons.Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Access Denied", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("You do not have permission to view reports.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Key Metrics
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val totalProfit by viewModel.totalProfit.collectAsState()
                    val weeklySales by viewModel.weeklySales.collectAsState()

                    ReportMetricCard(
                        title = "Today's Sales",
                        value = "₹${String.format("%.0f", totalSales)}",
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32)
                    )
                    ReportMetricCard(
                        title = "Today's Profit",
                        value = "₹${String.format("%.0f", totalProfit)}",
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFE3F2FD),
                        contentColor = Color(0xFF1976D2)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sales Trend Chart
                Text(
                    "SALES TREND (LAST 7 DAYS)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                val weeklySales by viewModel.weeklySales.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    com.kushwahahardware.ui.components.SalesChart(
                        salesData = weeklySales,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Inventory Insights
                Text(
                    "INVENTORY INSIGHTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = if (lowStockCount > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Low Stock Alert", fontWeight = FontWeight.SemiBold)
                                Text("$lowStockCount products need restocking", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}
