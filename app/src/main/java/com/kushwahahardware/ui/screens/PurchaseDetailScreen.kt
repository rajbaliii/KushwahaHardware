package com.kushwahahardware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.PurchaseDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(
    purchaseId: Long,
    viewModel: PurchaseDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val purchase by viewModel.purchase.collectAsState()
    val items by viewModel.items.collectAsState()
    val supplier by viewModel.supplier.collectAsState()
    val shopInfo by viewModel.shopInfo.collectAsState()

    LaunchedEffect(purchaseId) {
        viewModel.loadPurchase(purchaseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        purchase?.let { p ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Shop Info (Branding)
                Text(
                    text = shopInfo?.shopName ?: "KUSHWAHA HARDWARE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = shopInfo?.address ?: "Mahanwa, Bihar",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Invoice Details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Invoice No:", style = MaterialTheme.typography.labelSmall)
                        Text(p.invoiceNumber, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text("Date:", style = MaterialTheme.typography.labelSmall)
                        Text(dateFormat.format(Date(p.purchaseDate)), fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Supplier:", style = MaterialTheme.typography.labelSmall)
                Text(supplier?.name ?: "Unknown Supplier", fontWeight = FontWeight.Medium)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Items
                Text("Items Purchased", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f))
                            Text("\u20B9${item.totalPrice}", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Totals
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Bold)
                    Text("\u20B9${p.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid Amount")
                    Text("\u20B9${p.paidAmount}")
                }
                if (p.pendingAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pending Amount", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text("\u20B9${p.pendingAmount}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
