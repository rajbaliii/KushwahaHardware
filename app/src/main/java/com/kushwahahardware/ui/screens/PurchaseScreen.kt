package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.ui.viewmodel.PurchaseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel = hiltViewModel(),
    onNavigateToNewPurchase: () -> Unit,
    onNavigateToPurchaseDetail: (Long) -> Unit
) {
    val purchases by viewModel.purchases.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Purchase History") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewPurchase) {
                Icon(Icons.Default.Add, contentDescription = "New Purchase")
            }
        }
    ) { padding ->
        if (purchases.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No purchases yet. Tap '+' to add stock.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(purchases) { purchase ->
                    PurchaseItem(purchase = purchase, onClick = { onNavigateToPurchaseDetail(purchase.id) })
                }
            }
        }
    }
}

@Composable
fun PurchaseItem(purchase: Purchase, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
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
            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Invoice: ${purchase.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(purchase.purchaseDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "\u20B9${purchase.totalAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (purchase.pendingAmount > 0) {
                    Text(
                        text = "Due: \u20B9${purchase.pendingAmount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
