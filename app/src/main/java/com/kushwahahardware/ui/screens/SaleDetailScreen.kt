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
import com.kushwahahardware.ui.viewmodel.SaleDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    saleId: Long,
    viewModel: SaleDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val sale by viewModel.sale.collectAsState()
    val items by viewModel.items.collectAsState()
    val customer by viewModel.customer.collectAsState()
    val shopInfo by viewModel.shopInfo.collectAsState()

    LaunchedEffect(saleId) {
        viewModel.loadSale(saleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shareInvoice() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        sale?.let { s ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Shop Info
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
                        Text(s.invoiceNumber, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text("Date:", style = MaterialTheme.typography.labelSmall)
                        Text(dateFormat.format(Date(s.saleDate)), fontWeight = FontWeight.Medium)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Customer:", style = MaterialTheme.typography.labelSmall)
                Text(customer?.name ?: "Walking Customer", fontWeight = FontWeight.Medium)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Items
                Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Text("\u20B9${s.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid Amount")
                    Text("\u20B9${s.paidAmount}")
                }
                if (s.pendingAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pending Amount", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text("\u20B9${s.pendingAmount}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
                
                Button(
                    onClick = { viewModel.shareInvoice() },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Invoice via WhatsApp")
                }
            }
        }
    }
}
