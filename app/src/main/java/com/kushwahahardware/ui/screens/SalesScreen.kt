package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel = hiltViewModel(),
    onNavigateToNewSale: () -> Unit,
    onNavigateToSaleDetail: (Long) -> Unit
) {
    val sales by viewModel.sales.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sales History") })
        },
        floatingActionButton = {
            if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.SALES, com.kushwahahardware.security.AppActions.ADD)) {
                FloatingActionButton(onClick = onNavigateToNewSale) {
                    Icon(Icons.Default.Add, contentDescription = "New Sale")
                }
            }
        }
    ) { padding ->
        if (sales.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No sales yet. Tap '+' to create one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sales) { sale ->
                    SaleItem(sale = sale, onClick = { onNavigateToSaleDetail(sale.id) })
                }
            }
        }
    }
}

@Composable
fun SaleItem(sale: Sale, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Invoice: ${sale.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(sale.saleDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.REPORTS, com.kushwahahardware.security.AppActions.VIEW)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${sale.totalAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (sale.pendingAmount > 0) {
                        Text(
                            text = "Due: ₹${sale.pendingAmount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Paid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
