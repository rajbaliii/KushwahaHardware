package com.kushwahahardware.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Inventory : Screen("inventory")
    data object AddProduct : Screen("add_product?productId={productId}") {
        fun createRoute(productId: Long? = null) = 
            if (productId != null) "add_product?productId=$productId" else "add_product"
    }
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    data object Purchase : Screen("purchase")
    data object AddPurchase : Screen("add_purchase")
    data object PurchaseDetail : Screen("purchase_detail/{purchaseId}") {
        fun createRoute(purchaseId: Long) = "purchase_detail/$purchaseId"
    }
    data object Sales : Screen("sales")
    data object CreateSale : Screen("create_sale")
    data object SaleDetail : Screen("sale_detail/{saleId}") {
        fun createRoute(saleId: Long) = "sale_detail/$saleId"
    }
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
    data object ManageCategories : Screen("manage_categories")
}