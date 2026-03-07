package com.kushwahahardware.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kushwahahardware.navigation.NavGraph
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.navigation.bottomNavItems
import com.kushwahahardware.security.AppActions
import com.kushwahahardware.security.LocalPermissionManager
import com.kushwahahardware.security.rememberCanAccess
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

// Maps bottom nav screens to the permission required to see them
private fun screenPermission(screen: Screen): Pair<String, String>? = when (screen) {
    Screen.Dashboard  -> null  // Always visible
    Screen.Inventory  -> Pair("inventory", AppActions.VIEW)
    Screen.Sales      -> Pair("sales", AppActions.VIEW)
    Screen.Purchase   -> Pair("purchase", AppActions.VIEW)
    Screen.Reports    -> Pair("reports", AppActions.VIEW)
    Screen.Settings   -> null  // Always visible
    else              -> null
}

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val pm = LocalPermissionManager.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    // Check if user has permission to see this tab
                    val permPair = screenPermission(screen)
                    val canSee = if (permPair != null && pm != null) {
                        rememberCanAccess(permPair.first, permPair.second)
                    } else true

                    if (canSee) {
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = screen.icon, 
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(26.dp)
                                ) 
                            },
                            label = { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = screen.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (selected) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .height(2.5.dp)
                                                .background(Color(0xFF004D40), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF004D40),
                                selectedTextColor = Color(0xFF004D40),
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color(0xFF455A64),
                                unselectedTextColor = Color(0xFF455A64)
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            NavGraph(navController = navController, onLogout = onLogout)
        }
    }
}
