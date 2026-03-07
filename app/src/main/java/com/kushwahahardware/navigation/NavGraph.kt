package com.kushwahahardware.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kushwahahardware.ui.screens.*
import com.kushwahahardware.ui.screens.rbac.*

@Composable
fun NavGraph(navController: NavHostController, onLogout: () -> Unit = {}) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToNewSale = { navController.navigate("new_sale") },
                onNavigateToAddProduct = { navController.navigate("add_edit_product/0") },
                onNavigateToNewPurchase = { navController.navigate("new_purchase") },
                onNavigateToPendingDues = { navController.navigate(Screen.PendingDues.route) },
                onNavigateToInventory = { lowStock ->
                    navController.navigate("inventory?lowStock=$lowStock")
                }
            )
        }

        composable(
            route = Screen.Inventory.route,
            arguments = listOf(navArgument("lowStock") {
                type = NavType.BoolType
                defaultValue = false
            })
        ) { backStackEntry ->
            val lowStock = backStackEntry.arguments?.getBoolean("lowStock") ?: false
            InventoryScreen(
                initialLowStockFilter = lowStock,
                onNavigateToAddProduct = { navController.navigate("add_edit_product/0") },
                onNavigateToEditProduct = { id -> navController.navigate("add_edit_product/$id") }
            )
        }

        composable(
            route = "add_edit_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            AddEditProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Sales.route) {
            SalesScreen(
                onNavigateToNewSale = { navController.navigate("new_sale") },
                onNavigateToSaleDetail = { id -> navController.navigate("sale_detail/$id") }
            )
        }

        composable("new_sale") {
            NewSaleScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaleCompleted = { id ->
                    navController.navigate("sale_detail/$id") {
                        popUpTo("new_sale") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "sale_detail/{saleId}",
            arguments = listOf(navArgument("saleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
            SaleDetailScreen(
                saleId = saleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Purchase.route) {
            PurchaseScreen(
                onNavigateToNewPurchase = { navController.navigate("new_purchase") },
                onNavigateToPurchaseDetail = { id -> navController.navigate("purchase_detail/$id") }
            )
        }

        composable(
            route = "purchase_detail/{purchaseId}",
            arguments = listOf(navArgument("purchaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val purchaseId = backStackEntry.arguments?.getLong("purchaseId") ?: 0L
            PurchaseDetailScreen(
                purchaseId = purchaseId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NewPurchase.route) {
            NewPurchaseScreen(
                onNavigateBack = { navController.popBackStack() },
                onPurchaseCompleted = { _ -> navController.popBackStack() },
                onNavigateToAddParty = { type -> navController.navigate("add_party/$type") }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToUsersRoles = { navController.navigate(Screen.UsersRoles.route) },
                onNavigateToActivityLogs = { navController.navigate(Screen.ActivityLogs.route) },
                onNavigateToManagePermissions = { navController.navigate(Screen.ManagePermissions.route) },
                onLogout = onLogout
            )
        }

        composable(Screen.CustomerManagement.route) {
            CustomerManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomerDetail = { _ -> }
            )
        }

        composable(Screen.DealerManagement.route) {
            DealerManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSupplierDetail = { _ -> }
            )
        }

        composable(Screen.PendingDues.route) {
            PendingDuesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLedger = { id, type -> navController.navigate("party_ledger/$id/$type") },
                onNavigateToAddParty = { type -> navController.navigate("add_party/$type") }
            )
        }

        composable(
            route = Screen.AddParty.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "CUSTOMER"
            AddPartyScreen(
                initialType = type,
                onNavigateBack = { navController.popBackStack() },
                onPartyCreated = { id, partyType ->
                    navController.navigate("party_ledger/$id/$partyType") {
                        popUpTo("add_party/$type") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PartyLedger.route,
            arguments = listOf(
                navArgument("partyId") { type = NavType.LongType },
                navArgument("partyType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val partyId = backStackEntry.arguments?.getLong("partyId") ?: 0L
            val partyType = backStackEntry.arguments?.getString("partyType") ?: "CUSTOMER"
            PartyLedgerScreen(
                partyId = partyId,
                partyType = partyType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ──────── RBAC Screens ────────
        composable(Screen.UsersRoles.route) {
            UsersRolesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddUser = { navController.navigate(Screen.AddUser.route) },
                onNavigateToEditUser = { _ -> }
            )
        }

        composable(Screen.AddUser.route) {
            AddUserScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManagePermissions.route) {
            ManagePermissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ActivityLogs.route) {
            ActivityLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
