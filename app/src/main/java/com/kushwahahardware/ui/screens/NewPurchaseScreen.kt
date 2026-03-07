package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel

import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.ui.viewmodel.PurchaseCartItem
import com.kushwahahardware.ui.viewmodel.PurchaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPurchaseScreen(
    viewModel: PurchaseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPurchaseCompleted: (Long) -> Unit,
    onNavigateToAddParty: (String) -> Unit
) {

    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val products by viewModel.products.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val selectedSupplier by viewModel.selectedSupplier.collectAsState()
    val invoiceNumber by viewModel.invoiceNumber.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()

    var showProductPicker by remember { mutableStateOf(false) }
    var showSupplierPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Purchase Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "\u20B9${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { viewModel.savePurchase(onPurchaseCompleted) },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        enabled = cartItems.isNotEmpty() && selectedSupplier != null
                    ) {
                        Text("Save Purchase & Update Stock")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Invoice & Supplier Section
            OutlinedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { viewModel.setInvoiceNumber(it) },
                        label = { Text("Invoice Number") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showSupplierPicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSupplier?.name ?: "Select Supplier/Dealer *",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Supplier Details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            // Items Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Purchased Items", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showProductPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Item")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { item ->
                    PurchaseCartEntry(
                        item = item,
                        onRemove = { viewModel.removeFromCart(item) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (paidAmount == 0.0) "" else paidAmount.toString(),
                onValueChange = { viewModel.setPaidAmount(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Paid Amount") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("\u20B9") }
            )
        }

        if (showSupplierPicker) {
            SupplierPicker(
                suppliers = suppliers,
                onSupplierSelected = { 
                    viewModel.selectSupplier(it)
                    showSupplierPicker = false
                },
                onAddSupplier = { 
                    showSupplierPicker = false
                    onNavigateToAddParty("SUPPLIER")
                },
                onDismiss = { showSupplierPicker = false }
            )
        }


        if (showProductPicker) {
            PurchaseItemEntryDialog(
                products = products,
                onAdd = { product, qty, rate ->
                    viewModel.addToCart(product, qty, rate)
                    showProductPicker = false
                },
                onDismiss = { showProductPicker = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseItemEntryDialog(
    products: List<Product>,
    onAdd: (Product, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.sku?.contains(searchQuery, ignoreCase = true) == true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (selectedProduct == null) "Add Product to Purchase" else "Enter Quantity & Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedProduct == null) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Product") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(filteredProducts) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = { Text("Stock: ${product.currentStock} ${product.unit}") },
                                trailingContent = { Text("\u20B9${product.purchasePrice}") },
                                modifier = Modifier.clickable { 
                                    selectedProduct = product
                                    rate = product.purchasePrice.toString()
                                }
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(selectedProduct!!.name, fontWeight = FontWeight.Bold)
                            Text("Current Stock: ${selectedProduct!!.currentStock} ${selectedProduct!!.unit}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text(selectedProduct!!.unit) }
                    )
                    
                    OutlinedTextField(
                        value = rate,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) rate = it },
                        label = { Text("Purchase Rate (Per ${selectedProduct!!.unit})") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("\u20B9 ") }
                    )
                }
            }
        },
        confirmButton = {
            if (selectedProduct != null) {
                Button(
                    onClick = { 
                        val qty = quantity.toDoubleOrNull() ?: 0.0
                        val r = rate.toDoubleOrNull() ?: 0.0
                        if (qty > 0 && r > 0) {
                            onAdd(selectedProduct!!, qty, r)
                        }
                    },
                    enabled = quantity.isNotEmpty() && rate.isNotEmpty()
                ) {
                    Text("Add to List")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (selectedProduct == null) onDismiss() else selectedProduct = null }) {
                Text(if (selectedProduct == null) "Cancel" else "Back")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierPicker(
    suppliers: List<Supplier>,
    onSupplierSelected: (Supplier) -> Unit,
    onAddSupplier: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSuppliers = suppliers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Supplier")
                IconButton(onClick = onAddSupplier) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Supplier")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Supplier") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                
                if (filteredSuppliers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No suppliers found", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(filteredSuppliers) { supplier ->
                            ListItem(
                                headlineContent = { Text(supplier.name) },
                                supportingContent = { Text(supplier.phone) },
                                modifier = Modifier.clickable { onSupplierSelected(supplier) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAddSupplier) {
                Text("ADD NEW")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}


@Composable
fun PurchaseCartEntry(item: PurchaseCartItem, onRemove: () -> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.Medium)
            Text(
                "${item.quantity} ${item.product.unit} @ \u20B9${item.rate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text("\u20B9${item.total}", fontWeight = FontWeight.Bold)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}
