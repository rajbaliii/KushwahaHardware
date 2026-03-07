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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.ui.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
    viewModel: CustomerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Customers") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredCustomers) { customer ->
                    CustomerListItem(
                        customer = customer,
                        onClick = { onNavigateToCustomerDetail(customer.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { customer ->
                viewModel.saveCustomer(customer) {
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun CustomerListItem(customer: Customer, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        customer.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "New Customer" else "Edit Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.height(100.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Customer(id = customer?.id ?: 0, name = name, phone = phone, address = address))
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
