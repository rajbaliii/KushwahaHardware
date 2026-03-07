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
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.ui.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerManagementScreen(
    viewModel: SupplierViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSupplierDetail: (Long) -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSuppliers = suppliers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.businessName?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dealer Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.AddBusiness, contentDescription = "Add Dealer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Dealers/Suppliers") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredSuppliers) { supplier ->
                    SupplierListItem(
                        supplier = supplier,
                        onClick = { onNavigateToSupplierDetail(supplier.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditSupplierDialog(
            onDismiss = { showAddDialog = false },
            onSave = { supplier ->
                viewModel.saveSupplier(supplier) {
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun SupplierListItem(supplier: Supplier, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(supplier.businessName ?: supplier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Contact: ${supplier.phone}", style = MaterialTheme.typography.bodySmall)
                if (supplier.gstNumber != null) {
                    Text("GST: ${supplier.gstNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSupplierDialog(
    supplier: Supplier? = null,
    onDismiss: () -> Unit,
    onSave: (Supplier) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var businessName by remember { mutableStateOf(supplier?.businessName ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var gstNumber by remember { mutableStateOf(supplier?.gstNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supplier == null) "New Dealer" else "Edit Dealer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact Person *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                OutlinedTextField(value = gstNumber, onValueChange = { gstNumber = it }, label = { Text("GST Number") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Supplier(
                        id = supplier?.id ?: 0,
                        name = name,
                        businessName = businessName,
                        phone = phone,
                        gstNumber = gstNumber
                    ))
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
