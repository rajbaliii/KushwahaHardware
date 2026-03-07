package com.kushwahahardware.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.LedgerEntry
import com.kushwahahardware.ui.viewmodel.PartyLedgerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyLedgerScreen(
    partyId: Long,
    partyType: String,
    viewModel: PartyLedgerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val totalDues by viewModel.totalDues.collectAsState()
    val totalPaid by viewModel.totalPaid.collectAsState()
    val partyName by viewModel.partyName.collectAsState()
    
    var showRecordPayment by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(partyId, partyType) {
        viewModel.loadLedger(partyId, partyType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(partyName ?: "Party Ledger", fontWeight = FontWeight.Bold) },
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
        },
        bottomBar = {
            com.kushwahahardware.ui.screens.BottomPaymentBar(
                onYouGave = {
                    showRecordPayment = "GAVE"
                },
                onYouGot = {
                    showRecordPayment = "GOT"
                }
            )

        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Net Balance Header
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isCustomer = partyType == "CUSTOMER"
                    val labelText = if (isCustomer) {
                        if (netBalance >= 0) "You will get" else "You will give"
                    } else {
                        if (netBalance >= 0) "You will give" else "You will get"
                    }
                    val balanceColor = if (isCustomer) {
                        if (netBalance >= 0) Color(0xFF2E7D32) else Color(0xFFE53935)
                    } else {
                        if (netBalance >= 0) Color(0xFFE53935) else Color(0xFF2E7D32)
                    }

                    Text("Net Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = "₹${String.format("%.0f", Math.abs(netBalance))}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        color = balanceColor
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Dues", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₹${String.format("%.0f", totalDues)}", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₹${String.format("%.0f", totalPaid)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries) { entry ->
                    LedgerEntryItem(entry, partyType)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }

    if (showRecordPayment != null) {
        RecordPaymentDialogSnippet(
            type = showRecordPayment!!,
            onDismiss = { showRecordPayment = null },
            onConfirm = { amount ->
                viewModel.recordPayment(partyId, partyType, amount, showRecordPayment!!)
                showRecordPayment = null
            }
        )
    }
}

@Composable
fun LedgerEntryItem(entry: LedgerEntry, partyType: String) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val title = when (entry) {
                is LedgerEntry.Transaction -> entry.transaction.notes
                is LedgerEntry.SaleEntry -> "Sale: ${entry.invoiceNo}"
                is LedgerEntry.PurchaseEntry -> "Purchase: ${entry.invoiceNo}"
            }
            val date = when (entry) {
                is LedgerEntry.Transaction -> entry.transaction.transactionDate
                is LedgerEntry.SaleEntry -> entry.date
                is LedgerEntry.PurchaseEntry -> entry.date
            }
            Text(title ?: if (entry is LedgerEntry.Transaction) entry.transaction.transactionType else "No details", fontWeight = FontWeight.Medium)
            Text(dateFormat.format(Date(date)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        val amount = when (entry) {
            is LedgerEntry.Transaction -> entry.transaction.amount
            is LedgerEntry.SaleEntry -> entry.amount
            is LedgerEntry.PurchaseEntry -> entry.amount
        }

        val isRed = when (entry) {
            is LedgerEntry.SaleEntry -> true
            is LedgerEntry.PurchaseEntry -> true
            is LedgerEntry.Transaction -> {
                if (partyType == "CUSTOMER") {
                    entry.transaction.transactionType == "GAVE"
                } else {
                    entry.transaction.transactionType == "GOT"
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${String.format("%.0f", amount)}",
                fontWeight = FontWeight.Bold,
                color = if (isRed) Color(0xFFE53935) else Color(0xFF2E7D32)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialogSnippet(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val isGot = type == "GOT"
    val headerColor = if (isGot) Color(0xFF2E7D32) else Color(0xFFE53935)
    val title = if (isGot) "Receive Payment" else "Make Payment"
    val icon = if (isGot) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = headerColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = headerColor)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter the amount ${if (isGot) "received from" else "paid to"} the party.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amountText = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = headerColor,
                        focusedLabelColor = headerColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amountText.toDoubleOrNull()?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = headerColor),
                shape = RoundedCornerShape(8.dp),
                enabled = amountText.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("SAVE PAYMENT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}
