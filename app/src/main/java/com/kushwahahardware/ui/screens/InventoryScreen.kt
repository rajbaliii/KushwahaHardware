package com.kushwahahardware.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel


import com.kushwahahardware.R
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.Category
import com.kushwahahardware.utils.BarcodePdfGenerator


import com.kushwahahardware.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    initialLowStockFilter: Boolean = false,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val showLowStockOnly by viewModel.showLowStockOnly.collectAsState()

    LaunchedEffect(initialLowStockFilter) {
        if (initialLowStockFilter) {
            viewModel.setShowLowStockOnly(true)
        }
    }


    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val generatedBarcodes by viewModel.generatedBarcodes.collectAsState()

    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showBarcodeSuccess by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventory)) },
                actions = {
                    if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.INVENTORY, com.kushwahahardware.security.AppActions.EDIT)) {
                        IconButton(onClick = { showBarcodeDialog = true }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Generate Barcode")
                        }
                    }
                }
            )

        },
        floatingActionButton = {
            if (com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.INVENTORY, com.kushwahahardware.security.AppActions.ADD)) {
                FloatingActionButton(onClick = onNavigateToAddProduct) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Category Filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCatId == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text(stringResource(R.string.all)) }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCatId == category.id,
                        onClick = { viewModel.onCategorySelected(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            // Product List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    val canEdit = com.kushwahahardware.security.rememberCanAccess(com.kushwahahardware.security.AppModules.INVENTORY, com.kushwahahardware.security.AppActions.EDIT)
                    ProductItem(
                        product = product,
                        categoryName = categories.find { it.id == product.categoryId }?.name ?: stringResource(R.string.unknown),
                        onClick = { if (canEdit) onNavigateToEditProduct(product.id) }
                    )
                }
            }
        }

        if (showBarcodeDialog) {
            GenerateBarcodeDialog(
                onDismiss = { showBarcodeDialog = false },
                onGenerate = { count ->
                    viewModel.generateBarcodes(count) {
                        showBarcodeDialog = false
                        showBarcodeSuccess = true
                    }
                }
            )
        }

        if (showBarcodeSuccess) {
            AlertDialog(
                onDismissRequest = { showBarcodeSuccess = false },
                title = { Text("Success") },
                text = { Text("${generatedBarcodes.size} barcodes generated successfully. They are stored in the database for your use.") },
                confirmButton = {
                    Button(onClick = { 
                        val path = BarcodePdfGenerator.generateBarcodePdf(context, generatedBarcodes)
                        if (path != null) {
                            android.widget.Toast.makeText(context, "PDF saved to Downloads", android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to create PDF", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showBarcodeSuccess = false 
                    }) {
                        Text("Download PDF")
                    }
                },


                dismissButton = {
                    TextButton(onClick = { showBarcodeSuccess = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun GenerateBarcodeDialog(
    onDismiss: () -> Unit,
    onGenerate: (Int) -> Unit
) {
    var countText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Barcode") },
        text = {
            Column {
                Text("Enter number of unique barcodes to generate:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = countText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) countText = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

            }
        },
        confirmButton = {
            Button(
                onClick = { countText.toIntOrNull()?.let { onGenerate(it) } },
                enabled = countText.isNotEmpty() && (countText.toIntOrNull() ?: 0) > 0
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun ProductItem(
    product: Product,
    categoryName: String,
    onClick: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$categoryName | SKU: ${product.sku ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${product.sellingPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Margin display
                if (product.sellingPrice > 0) {
                    val margin = ((product.sellingPrice - product.purchasePrice) / product.sellingPrice) * 100
                    val marginColor = if (margin >= 20) Color(0xFF2E7D32) else if (margin >= 10) Color(0xFFFFA000) else Color(0xFFC62828)
                    Text(
                        text = "${String.format("%.1f", margin)}% Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = marginColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                val statusColor = if (product.currentStock <= product.minStockLevel) 
                    MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.secondary
                Text(
                    text = "${stringResource(R.string.stock)}: ${product.currentStock} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
