package com.kushwahahardware.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.ui.viewmodel.PendingDue
import com.kushwahahardware.ui.viewmodel.PendingDuesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingDuesScreen(
    viewModel: PendingDuesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLedger: (Long, String) -> Unit,
    onNavigateToAddParty: (String) -> Unit
) {

    val pendingDues by viewModel.pendingDues.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("CUSTOMERS", "SUPPLIERS")

    // Payment dialog state
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedDue by remember { mutableStateOf<PendingDue?>(null) }
    var paymentType by remember { mutableStateOf("GOT") } // "GOT" or "GAVE"

    val customerDues = pendingDues.filter { it.type == "CUSTOMER" }
    val supplierDues = pendingDues.filter { it.type == "SUPPLIER" }

    // Summary
    val totalToGet = customerDues.sumOf { it.amount }
    val totalToGive = supplierDues.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Book", fontWeight = FontWeight.Bold) },
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
            // "YOU GAVE / YOU GOT" bottom action bar removed as per request
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddParty(if (selectedTab == 0) "CUSTOMER" else "SUPPLIER") },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("ADD ${if (selectedTab == 0) "CUSTOMER" else "SUPPLIER"}") },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Header (blue banner)
            AccountSummaryHeader(
                totalToGet = if (selectedTab == 0) totalToGet else 0.0,
                totalToGive = if (selectedTab == 1) totalToGive else 0.0,
                isSupplierTab = selectedTab == 1
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFFC107)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White
                            )
                        }
                    )
                }
            }

            // Party list
            val currentDues = if (selectedTab == 0) customerDues else supplierDues

            if (currentDues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No ${if (selectedTab == 0) "customers" else "suppliers"} added yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }

            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(currentDues, key = { it.id }) { due ->
                        AccountPartyItem(
                            due = due,
                            onSendReminder = {
                                val message = if (due.type == "CUSTOMER") {
                                    "Dear ${due.name}, this is a reminder from Kushwaha Hardware. " +
                                    "Your outstanding balance is ₹${String.format("%.0f", due.amount)}. " +
                                    "Please settle it at your earliest convenience. Thank you!"
                                } else {
                                    "Hello ${due.name}, regarding our pending payment of " +
                                    "₹${String.format("%.0f", due.amount)}. " +
                                    "Please share your account details or UPI for settlement. Thanks, Kushwaha Hardware."
                                }
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=91${due.phone}&text=${Uri.encode(message)}")
                                }
                                try { context.startActivity(intent) } catch (e: Exception) { }
                            },
                            onRecordGot = {
                                selectedDue = due
                                paymentType = "GOT"
                                showPaymentDialog = true
                            },
                            onRecordGave = {
                                selectedDue = due
                                paymentType = "GAVE"
                                showPaymentDialog = true
                            },
                            onItemClick = { onNavigateToLedger(due.id, due.type) }
                        )
                    }

                }
            }
        }
    }

    // Payment recording dialog
    if (showPaymentDialog && selectedDue != null) {
        RecordPaymentDialog(
            due = selectedDue!!,
            paymentType = paymentType,
            allDues = if (selectedTab == 0) customerDues else supplierDues,
            onDismiss = {
                showPaymentDialog = false
                selectedDue = null
            },
            onConfirm = { due, amount, type ->
                viewModel.recordPayment(due, amount, type)
                showPaymentDialog = false
                selectedDue = null
            }
        )
    }
}

@Composable
fun AccountSummaryHeader(totalToGet: Double, totalToGive: Double, isSupplierTab: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1565C0))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSupplierTab) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text("Total Give", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    "₹${String.format("%.0f", totalToGive)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (totalToGive > 0) Color(0xFFFF8A65) else Color.White
                )
            }
        }
        
        if (!isSupplierTab) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                Text("Total Get", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(
                    "₹${String.format("%.0f", totalToGet)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (totalToGet > 0) Color(0xFF69F0AE) else Color.White
                )
            }
        }
    }
}



@Composable
fun AccountPartyItem(
    due: PendingDue,
    onSendReminder: () -> Unit,
    onRecordGot: () -> Unit,
    onRecordGave: () -> Unit,
    onItemClick: () -> Unit
) {
    val isCustomer = due.type == "CUSTOMER"
    
    // Logic for labels and color based on party type and balance direction
    val (labelText, amountColor) = if (isCustomer) {
        if (due.amount >= 0) "You will get" to Color(0xFF2E7D32) // Green
        else "You will give" to Color(0xFFE53935) // Red
    } else {
        if (due.amount >= 0) "You will give" to Color(0xFFE53935) // Red
        else "You will get" to Color(0xFF2E7D32) // Green
    }

    val avatarColor = if (isCustomer) Color(0xFF1565C0) else Color(0xFF6A1B9A)
    val initials = due.name.split(" ").take(2).map { it.firstOrNull()?.uppercaseChar() ?: ' ' }.joinToString("")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onItemClick() },

        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and contact
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = due.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = due.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Amount + Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.0f", Math.abs(due.amount))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = amountColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BottomPaymentBar(onYouGave: () -> Unit, onYouGot: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onYouGave,
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("YOU GAVE ₹", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Button(
            onClick = onYouGot,
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("YOU GOT ₹", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    due: PendingDue,
    paymentType: String,
    allDues: List<PendingDue>,
    onDismiss: () -> Unit,
    onConfirm: (PendingDue, Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedParty by remember { mutableStateOf(due) }
    var expanded by remember { mutableStateOf(false) }

    val isGot = paymentType == "GOT"
    val headerColor = if (isGot) Color(0xFF2E7D32) else Color(0xFFE53935)
    val title = if (isGot) "You Got ₹" else "You Gave ₹"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = headerColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Party selector dropdown
                if (allDues.size > 1) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedParty.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Party") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            allDues.forEach { d ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(d.name, modifier = Modifier.weight(1f))
                                            Text(
                                                "₹${String.format("%.0f", d.amount)}",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedParty = d
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Show party name as label
                    Text(
                        text = "Party: ${selectedParty.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Outstanding: ₹${String.format("%.0f", selectedParty.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) amountText = v },
                    label = { Text("Enter Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(selectedParty, amount, paymentType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = headerColor),
                enabled = amountText.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Keep backward-compat composable (used elsewhere)
@Composable
fun PendingDueItem(due: PendingDue, onSendReminder: () -> Unit) {
    AccountPartyItem(
        due = due,
        onSendReminder = onSendReminder,
        onRecordGot = {},
        onRecordGave = {},
        onItemClick = {}
    )
}
