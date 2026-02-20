package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.ui.theme.*
import com.kushwahahardware.ui.viewmodel.PurchaseItemData
import com.kushwahahardware.ui.viewmodel.PurchaseViewModel
import com.kushwahahardware.utils.CurrencyUtils
import com.kushwahahardware.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    navController: NavController,
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddPurchase.route) },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Purchase")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Purchases") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Suppliers") }
                )
            }
            
            when (selectedTab) {
                0 -> PurchasesList(
                    purchases = uiState.purchases,
                    isLoading = uiState.isLoading
                )
                1 -> SuppliersList(
                    suppliers = uiState.suppliers,
                    onAddSupplier = { /* Show add supplier dialog */ }
                )
            }
        }
    }
}

@Composable
fun PurchasesList(
    purchases: List<com.kushwahahardware.data.entity.PurchaseWithItems>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (purchases.isEmpty()) {
        EmptyState(message = "No purchases yet")
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(purchases) { purchaseWithItems ->
                PurchaseCard(purchaseWithItems = purchaseWithItems)
            }
        }
    }
}

@Composable
fun PurchaseCard(purchaseWithItems: com.kushwahahardware.data.entity.PurchaseWithItems) {
    val purchase = purchaseWithItems.purchase
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = purchase.invoiceNumber.ifEmpty { "#${purchase.id}" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDate(purchase.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatWithSymbol(purchase.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    if (purchase.pendingAmount > 0) {
                        Text(
                            text = "Pending: ${CurrencyUtils.formatWithSymbol(purchase.pendingAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${purchaseWithItems.items.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SuppliersList(
    suppliers: List<Supplier>,
    onAddSupplier: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Supplier")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (suppliers.isEmpty()) {
            EmptyState(message = "No suppliers yet")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suppliers) { supplier ->
                    SupplierCard(supplier = supplier)
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onSave = { supplier ->
                onAddSupplier()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SupplierCard(supplier: Supplier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (supplier.phone.isNotEmpty()) {
                    Text(
                        text = supplier.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            if (supplier.totalDue > 0) {
                Text(
                    text = CurrencyUtils.formatWithSymbol(supplier.totalDue),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onSave: (Supplier) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Supplier(name = name, phone = phone, address = address))
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    navController: NavController,
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val purchaseItems by viewModel.purchaseItems.collectAsState()
    
    var selectedSupplierId by remember { mutableStateOf<Long?>(null) }
    var invoiceNumber by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Purchase") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            PurchaseBottomBar(
                total = uiState.currentTotal,
                paid = paidAmount.toDoubleOrNull() ?: 0.0,
                pending = uiState.currentPending,
                onSave = {
                    viewModel.savePurchase(
                        supplierId = selectedSupplierId,
                        date = System.currentTimeMillis(),
                        invoiceNumber = invoiceNumber
                    )
                    navController.navigateUp()
                }
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
            item {
                // Supplier Selection
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.suppliers.find { it.id == selectedSupplierId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Supplier") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.suppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.name) },
                                onClick = {
                                    selectedSupplierId = supplier.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { Text("Invoice Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { 
                        paidAmount = it
                        viewModel.setPaidAmount(it.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Paid Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            itemsIndexed(purchaseItems) { index, item ->
                PurchaseItemRow(
                    item = item,
                    onRemove = { viewModel.removePurchaseItem(index) }
                )
            }
            
            item {
                AddPurchaseItemButton(
                    products = uiState.products,
                    onAdd = { product, qty, price ->
                        viewModel.addPurchaseItem(product, qty, price)
                    }
                )
            }
        }
    }
}

@Composable
fun PurchaseItemRow(
    item: PurchaseItemData,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${item.quantity} x ${CurrencyUtils.formatWithSymbol(item.purchasePrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = CurrencyUtils.formatWithSymbol(item.quantity * item.purchasePrice),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Error)
            }
        }
    }
}

@Composable
fun AddPurchaseItemButton(
    products: List<com.kushwahahardware.data.entity.Product>,
    onAdd: (com.kushwahahardware.data.entity.Product, Int, Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Item")
    }
    
    if (showDialog) {
        AddPurchaseItemDialog(
            products = products,
            onDismiss = { showDialog = false },
            onAdd = { product, qty, price ->
                onAdd(product, qty, price)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddPurchaseItemDialog(
    products: List<com.kushwahahardware.data.entity.Product>,
    onDismiss: () -> Unit,
    onAdd: (com.kushwahahardware.data.entity.Product, Int, Double) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<com.kushwahahardware.data.entity.Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Product *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    selectedProduct = product
                                    price = product.purchasePrice.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Purchase Price *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        onAdd(
                            product,
                            quantity.toIntOrNull() ?: 1,
                            price.toDoubleOrNull() ?: product.purchasePrice
                        )
                    }
                },
                enabled = selectedProduct != null && quantity.isNotBlank() && price.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PurchaseBottomBar(
    total: Double,
    paid: Double,
    pending: Double,
    onSave: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", fontWeight = FontWeight.Medium)
                Text(
                    CurrencyUtils.formatWithSymbol(total),
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Paid:", fontWeight = FontWeight.Medium)
                Text(CurrencyUtils.formatWithSymbol(paid))
            }
            if (pending > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pending:", fontWeight = FontWeight.Medium)
                    Text(
                        CurrencyUtils.formatWithSymbol(pending),
                        color = Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = total > 0
            ) {
                Text("Save Purchase")
            }
        }
    }
}