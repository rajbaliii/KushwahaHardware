package com.kushwahahardware.ui.screens.rbac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.data.entity.ActivityLog
import com.kushwahahardware.security.AppActions
import com.kushwahahardware.security.AppModules
import com.kushwahahardware.security.rememberCanAccess
import com.kushwahahardware.ui.viewmodel.ActivityLogsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    viewModel: ActivityLogsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val logs by viewModel.logs.collectAsState()
    val canView = rememberCanAccess(AppModules.ACTIVITY_LOGS, AppActions.VIEW)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        if (!canView) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Access Denied",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You do not have permission to view activity logs.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No activity logs yet", color = Color.Gray, fontSize = 16.sp)
                    Text("Actions you take will appear here", color = Color.LightGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs) { log ->
                    ActivityLogItem(log)
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLog) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(log.timestamp))

    val avatarColors = listOf(
        Color(0xFF1976D2), Color(0xFF00897B), Color(0xFFEF6C00),
        Color(0xFF6A1B9A), Color(0xFF2E7D32)
    )
    val avatarColor =
        avatarColors[log.userName.hashCode().let { if (it < 0) -it else it } % avatarColors.size]

    val (badgeColor, badgeLabel) = when {
        log.action.lowercase().contains("add") || log.action.lowercase().contains("create") ->
            Color(0xFF2E7D32) to "ADD"
        log.action.lowercase().contains("delete") ->
            Color(0xFFC62828) to "DELETE"
        log.action.lowercase().contains("edit") || log.action.lowercase().contains("update") ->
            Color(0xFFE65100) to "EDIT"
        log.action.lowercase().contains("login") ->
            Color(0xFF1976D2) to "LOGIN"
        log.action.lowercase().contains("logout") ->
            Color(0xFF7B1FA2) to "LOGOUT"
        else -> Color(0xFF455A64) to log.action.uppercase().take(8)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = log.userName.take(1).uppercase(),
                    color = avatarColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Action pill
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    // Module pill
                    Surface(
                        color = Color(0xFFF0F0F0),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = log.module.replaceFirstChar { it.uppercase() },
                            color = Color(0xFF616161),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = log.details,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    lineHeight = 17.sp
                )
            }
        }
    }
}
