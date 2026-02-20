package com.kushwahahardware.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )
    data object Inventory : BottomNavItem(
        route = Screen.Inventory.route,
        title = "Inventory",
        icon = Icons.Default.Inventory
    )
    data object Purchase : BottomNavItem(
        route = Screen.Purchase.route,
        title = "Purchase",
        icon = Icons.Default.ShoppingCart
    )
    data object Sales : BottomNavItem(
        route = Screen.Sales.route,
        title = "Sales",
        icon = Icons.Default.PointOfSale
    )
    data object Reports : BottomNavItem(
        route = Screen.Reports.route,
        title = "Reports",
        icon = Icons.Default.Assessment
    )
    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        icon = Icons.Default.Settings
    )
    
    companion object {
        val items = listOf(Dashboard, Inventory, Purchase, Sales, Reports, Settings)
    }
}