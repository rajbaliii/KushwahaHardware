package com.kushwahahardware.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kushwahahardware.data.entity.PaymentType
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.ui.theme.*
import com.kushwahahardware.ui.viewmodel.SaleItemData
import com.kushwahahardware.ui.viewmodel.SalesViewModel
import com.kushwahahardware.utils.CurrencyUtils
import com.kushwahahardware.utils.DateUtils
import com.kushwahahardware.utils.PdfGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateSale.route) },
                containerColor = Success
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Sale")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchSales(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by customer or invoice…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.sales.isEmpty()) {
                EmptyState(message = "No sales yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.sales) { saleWithItems ->
                        SaleCard(
                            sale = saleWithItems.sale,
                            onClick = { 
                                navController.navigate(Screen.SaleDetail.createRoute(saleWithItems.sale.id))
                            },
                            onShare = { shareInvoice(saleWithItems.sale, saleWithItems.items) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: Sale,
    onClick: () -> Unit,
    onShare: () -> Unit
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.getFormattedInvoiceNumber(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDate(sale.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (sale.customerName.isNotEmpty()) {
                        Text(
                            text = sale.customerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatWithSymbol(sale.totalAmount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                    Text(
                        text = sale.paymentType.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sale.paymentType == PaymentType.CASH) Success else Warning
                    )
                    if (sale.pendingAmount > 0) {
                        Text(
                            text = "Pending: ${CurrencyUtils.formatWithSymbol(sale.pendingAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun shareInvoice(sale: Sale, items: List<com.kushwahahardware.data.entity.SaleItem>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    scope.launch {
        val shopInfo = com.kushwahahardware.KushwahaHardwareApp.instance.let {
            com.kushwahahardware.data.database.AppDatabase.getDatabase(it).shopInfoDao().getShopInfoSync()
        } ?: com.kushwahahardware.data.entity.ShopInfo()
        
        val pdfFile = PdfGenerator.generateInvoice(context, sale, items, shopInfo)
        pdfFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice ${sale.getFormattedInvoiceNumber()}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sale") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            SaleBottomBar(
                total = uiState.currentTotal,
                paid = paidAmount.toDoubleOrNull() ?: 0.0,
                pending = uiState.currentPending,
                paymentType = uiState.currentPaymentType,
                onSave = {
                    scope.launch {
                        val sale = viewModel.saveSale(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            date = System.currentTimeMillis()
                        )
                        sale?.let {
                            // Generate and share PDF
                            val shopInfo = com.kushwahahardware.KushwahaHardwareApp.instance.let {
                                com.kushwahahardware.data.database.AppDatabase.getDatabase(it).shopInfoDao().getShopInfoSync()
                            } ?: com.kushwahahardware.data.entity.ShopInfo()
                            
                            val items = saleItems.map { item ->
                                com.kushwahahardware.data.entity.SaleItem(
                                    saleId = it.id,
                                    productId = item.product.id,
                                    productName = item.product.name,
                                    quantity = item.quantity,
                                    sellingPrice = item.sellingPrice,
                                    totalAmount = item.quantity * item.sellingPrice
                                )
                            }
                            
                            val pdfFile = PdfGenerator.generateInvoice(context, it, items, shopInfo)
                            pdfFile?.let { file ->
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Invoice ${it.getFormattedInvoiceNumber()}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                
                                context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                            }
                        }
                        navController.navigateUp()
                    }
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
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            item {
                Text(
                    text = "Payment Type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.currentPaymentType == PaymentType.CASH,
                        onClick = { viewModel.setPaymentType(PaymentType.CASH) },
                        label = { Text("Cash") }
                    )
                    FilterChip(
                        selected = uiState.currentPaymentType == PaymentType.CREDIT,
                        onClick = { viewModel.setPaymentType(PaymentType.CREDIT) },
                        label = { Text("Credit") }
                    )
                }
            }
            
            if (uiState.currentPaymentType == PaymentType.CREDIT) {
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
            }
            
            item {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            itemsIndexed(saleItems) { index, item ->
                SaleItemRow(
                    item = item,
                    onRemove = { viewModel.removeSaleItem(index) }
                )
            }
            
            item {
                AddSaleItemButton(
                    products = uiState.products,
                    onAdd = { product, qty ->
                        viewModel.addSaleItem(product, qty)
                    }
                )
            }
        }
    }
}

@Composable
fun SaleItemRow(
    item: SaleItemData,
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
                    text = "${item.quantity} x ${CurrencyUtils.formatWithSymbol(item.sellingPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = CurrencyUtils.formatWithSymbol(item.quantity * item.sellingPrice),
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
fun AddSaleItemButton(
    products: List<com.kushwahahardware.data.entity.Product>,
    onAdd: (com.kushwahahardware.data.entity.Product, Int) -> Unit
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
        AddSaleItemDialog(
            products = products,
            onDismiss = { showDialog = false },
            onAdd = { product, qty ->
                onAdd(product, qty)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddSaleItemDialog(
    products: List<com.kushwahahardware.data.entity.Product>,
    onDismiss: () -> Unit,
    onAdd: (com.kushwahahardware.data.entity.Product, Int) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<com.kushwahahardware.data.entity.Product?>(null) }
    var quantity by remember { mutableStateOf("1") }
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
                        products.filter { it.currentStock > 0 }.forEach { product ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(product.name)
                                        Text(
                                            "Stock: ${product.currentStock} | ${CurrencyUtils.formatWithSymbol(product.sellingPrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = product
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
                
                selectedProduct?.let { product ->
                    Text(
                        text = "Available: ${product.currentStock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Price: ${CurrencyUtils.formatWithSymbol(product.sellingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedProduct?.let { product ->
                        onAdd(product, quantity.toIntOrNull() ?: 1)
                    }
                },
                enabled = selectedProduct != null && quantity.isNotBlank()
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
fun SaleBottomBar(
    total: Double,
    paid: Double,
    pending: Double,
    paymentType: PaymentType,
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
                    color = Success
                )
            }
            
            if (paymentType == PaymentType.CREDIT) {
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
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = total > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Success)
            ) {
                Text("Save & Share Invoice")
            }
        }
    }
}