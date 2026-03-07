package com.kushwahahardware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.ui.components.BarcodeScanner
import com.kushwahahardware.ui.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ArrowDropDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long = 0,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val product by viewModel.product.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var currentStock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }

    var showScanner by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf<Product?>(null) }
    
    val units = listOf("pcs", "kg", "g", "liter", "ml", "meter", "cm", "box", "pack")
    val decimalUnits = listOf("kg", "g", "liter", "ml", "meter", "cm")

    LaunchedEffect(productId) {
        if (productId != 0L) {
            viewModel.loadProduct(productId)
        }
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            sku = it.sku ?: ""
            categoryId = it.categoryId
            purchasePrice = it.purchasePrice.toString()
            sellingPrice = it.sellingPrice.toString()
            currentStock = it.currentStock.toString()
            minStock = it.minStockLevel.toString()
            unit = it.unit
        }
    }

    if (showScanner) {
        AlertDialog(
            onDismissRequest = { showScanner = false },
            title = { Text("Scan Barcode") },
            text = {
                Box(modifier = Modifier.size(300.dp)) {
                    BarcodeScanner(onBarcodeScanned = { scannedCode ->
                        sku = scannedCode
                        showScanner = false
                        // Check for duplicate
                        kotlinx.coroutines.MainScope().launch {
                            val existing = viewModel.getProductBySku(scannedCode)
                            if (existing != null && existing.id != productId) {
                                showDuplicateDialog = existing
                            }
                        }
                    })
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanner = false }) { Text("Cancel") }
            }
        )
    }

    showDuplicateDialog?.let { existing ->
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = null },
            title = { Text("Product already exists") },
            text = { Text("Product '${existing.name}' with this barcode already exists. Do you want to update its stock instead?") },
            confirmButton = {
                Button(onClick = {
                    showDuplicateDialog = null
                    // Navigate to edit or show stock update (for simplicity, we'll justToast for now or we could navigate)
                    // In a real POS, we'd open a stock update dialog.
                }) { Text("Update Stock") }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == 0L) "Add Product" else "Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveProduct(
                            id = productId,
                            name = name,
                            sku = sku,
                            categoryId = categoryId,
                            purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                            currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                            minStock = minStock.toDoubleOrNull() ?: 0.0,
                            unit = unit,
                            onSuccess = onNavigateBack
                        )
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Barcode") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                    }
                }
            )

            // Category Selection
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = categories.find { it.id == categoryId }?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                categoryId = category.id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Purchase Price") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price *") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Unit Selection with Dynamic Keyboard Logic
            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (pcs, kg, m...)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                unit = u
                                unitExpanded = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("+ Add Custom Unit") },
                        onClick = {
                            unitExpanded = false
                            // In a real app, show a dialog to enter custom unit
                        }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentStock,
                    onValueChange = { currentStock = it },
                    label = { Text("Current Stock") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (unit in decimalUnits) KeyboardType.Decimal else KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Min Stock Alert") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (unit in decimalUnits) KeyboardType.Decimal else KeyboardType.Number
                    )
                )
            }
            
            Button(
                onClick = {
                    viewModel.saveProduct(
                        id = productId,
                        name = name,
                        sku = sku,
                        categoryId = categoryId,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                        minStock = minStock.toDoubleOrNull() ?: 0.0,
                        unit = unit,
                        onSuccess = onNavigateBack
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = name.isNotBlank() && sellingPrice.isNotBlank()
            ) {
                Text("Save Product")
            }
        }
    }
}
