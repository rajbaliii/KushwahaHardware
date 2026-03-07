package com.kushwahahardware.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Inventory : Screen("inventory?lowStock={lowStock}", "Inventory", Icons.Default.Inventory)

    object Sales : Screen("sales", "Sales", Icons.Default.TrendingUp)
    object Purchase : Screen("purchase", "Purchases", Icons.Default.ShoppingCart)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Person)
    
    // Non-Bottom Nav Screens
    object NewSale : Screen("new_sale", "New Sale", Icons.Default.AddShoppingCart)
    object SaleDetail : Screen("sale_detail/{saleId}", "Sale Detail", Icons.Default.Description)
    object NewPurchase : Screen("new_purchase", "New Purchase", Icons.Default.AddBusiness)
    
    // Management Screens
    object CustomerManagement : Screen("customer_management", "Customers", Icons.Default.People)
    object DealerManagement : Screen("dealer_management", "Dealers", Icons.Default.Store)
    object PendingDues : Screen("pending_dues", "Pending Dues", Icons.Default.AccountBalanceWallet)
    
    // Ledger
    object PartyLedger : Screen("party_ledger/{partyId}/{partyType}", "Party Ledger", Icons.Default.History)
    object AddParty : Screen("add_party/{type}", "Add Party", Icons.Default.PersonAdd)

    // RBAC
    object UsersRoles : Screen("users_roles", "Users & Roles", Icons.Default.Group)
    object ManagePermissions : Screen("manage_permissions", "Manage Permissions", Icons.Default.Security)
    object ActivityLogs : Screen("activity_logs", "Activity Logs", Icons.Default.Assignment)
    object AddUser : Screen("add_user", "Add User", Icons.Default.PersonAdd)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Inventory,
    Screen.Sales,
    Screen.Purchase,
    Screen.Settings
)
