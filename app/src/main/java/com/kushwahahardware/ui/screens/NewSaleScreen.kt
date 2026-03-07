package com.kushwahahardware.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.R
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.utils.PdfGenerator


import com.kushwahahardware.ui.viewmodel.CartItem
import com.kushwahahardware.ui.viewmodel.NewSaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    viewModel: NewSaleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaleCompleted: (Long) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()

    var showProductPicker by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showQrPayment by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_sale)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.scan_barcode))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${stringResource(R.string.summary)}:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "\u20B9${String.format("%.2f", totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showQrPayment = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate QR")
                        }
                        
                        Button(
                            onClick = { viewModel.saveSale(onSaleCompleted) },
                            modifier = Modifier.weight(1.5f).height(56.dp),
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Complete Sale & Invoice")
                        }
                    }
                }
            }

        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Customer Selector
            OutlinedCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedCustomer?.name ?: stringResource(R.string.walking_customer),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.customer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cart Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.cart_items), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { showProductPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.add_item))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { item ->
                    CartEntry(
                        item = item,
                        onRemove = { viewModel.removeFromCart(item) }
                    )
                }
            }
        }

        if (showProductPicker) {
            ProductPicker(
                products = products,
                onProductSelected = { product ->
                    viewModel.addToCart(product, 1.0)
                    showProductPicker = false
                },
                onDismiss = { showProductPicker = false }
            )
        }

        if (showQrPayment) {
            QrPaymentDialog(
                amount = totalAmount,
                onDismiss = { showQrPayment = false }
            )
        }

        if (showScanner) {
            AlertDialog(
                onDismissRequest = { showScanner = false },
                title = { Text(stringResource(R.string.scan_product)) },
                text = {
                    Box(modifier = Modifier.height(300.dp)) {
                        com.kushwahahardware.ui.components.BarcodeScanner(
                            onBarcodeScanned = { barcode ->
                                viewModel.onBarcodeScanned(barcode, products)
                                showScanner = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showScanner = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}


@Composable
fun QrPaymentDialog(amount: Double, onDismiss: () -> Unit) {
    val upiId = "BHARATPE.8F0V1P1U0V14772@fbpe"
    val name = "Mr Rajbali Kumar"


    val upiUrl = "upi://pay?pa=$upiId&pn=${name.replace(" ", "%20")}&am=$amount&cu=INR"
    
    val qrBitmap = remember(upiUrl) {
        PdfGenerator.generateQRCode(upiUrl, 512)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Instant Bill Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Scan to pay \u20B9${String.format("%.2f", amount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Payment QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(150.dp))
                    }
                }

                
                Text(
                    "UPI ID: $upiId",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}


@Composable
fun CartEntry(item: CartItem, onRemove: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPicker(
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_product)) },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(products) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = { Text("Stock: ${product.currentStock}") },
                        trailingContent = { Text("\u20B9${product.sellingPrice}") },
                        modifier = Modifier.clickable { onProductSelected(product) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
