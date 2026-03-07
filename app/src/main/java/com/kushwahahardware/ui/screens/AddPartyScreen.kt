package com.kushwahahardware.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.PartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartyScreen(
    initialType: String = "CUSTOMER",
    viewModel: PartyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPartyCreated: (Long, String) -> Unit
) {
    var partyType by remember { mutableStateOf(initialType) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }

    val isCustomer = partyType == "CUSTOMER"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCustomer) "Add Customer" else "Add Supplier") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isCustomer,
                    onClick = { partyType = "CUSTOMER" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Customer")
                }
                SegmentedButton(
                    selected = !isCustomer,
                    onClick = { partyType = "SUPPLIER" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Supplier")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phone = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("+91 ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            if (!isCustomer) {
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it.uppercase() },
                    label = { Text("GSTIN (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveParty(partyType, name, phone, address, gstin) { id ->
                        onPartyCreated(id, partyType)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && phone.length == 10,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text("SAVE $partyType", fontWeight = FontWeight.Bold)
            }
        }
    }
}
