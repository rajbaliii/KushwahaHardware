package com.kushwahahardware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kushwahahardware.navigation.BottomNavItem
import com.kushwahahardware.navigation.Screen
import com.kushwahahardware.ui.screens.*
import com.kushwahahardware.ui.theme.KushwahaHardwareTheme
import com.kushwahahardware.ui.theme.Primary
import com.kushwahahardware.ui.viewmodel.SettingsViewModel
import com.kushwahahardware.utils.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var biometricHelper: BiometricHelper
    
    private val settingsViewModel: SettingsViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setContent {
            KushwahaHardwareTheme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var showBiometricPrompt by remember { mutableStateOf(false) }
                
                val shopInfo by settingsViewModel.uiState.collectAsState()
                
                // Check if biometric is enabled
                LaunchedEffect(shopInfo.shopInfo.biometricEnabled) {
                    if (shopInfo.shopInfo.biometricEnabled && biometricHelper.canAuthenticate()) {
                        showBiometricPrompt = true
                    } else {
                        isAuthenticated = true
                    }
                }
                
                // Show biometric prompt
                if (showBiometricPrompt && !isAuthenticated) {
                    LaunchedEffect(Unit) {
                        biometricHelper.showBiometricPrompt(
                            activity = this@MainActivity,
                            onSuccess = {
                                isAuthenticated = true
                                showBiometricPrompt = false
                            },
                            onError = { error ->
                                // Handle error - maybe show PIN entry
                                isAuthenticated = true // Allow access for now
                                showBiometricPrompt = false
                            },
                            onCancel = {
                                // User cancelled - close app or show PIN
                                finish()
                            }
                        )
                    }
                }
                
                if (isAuthenticated) {
                    MainScreen()
                } else {
                    // Show loading or splash
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Hide bottom bar for certain screens
    val showBottomBar = currentDestination?.route in BottomNavItem.items.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            
            composable(Screen.Inventory.route) {
                InventoryScreen(navController = navController)
            }
            
            composable(Screen.AddProduct.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                // Handle edit product
            }
            
            composable(Screen.Purchase.route) {
                PurchaseScreen(navController = navController)
            }
            
            composable(Screen.AddPurchase.route) {
                AddPurchaseScreen(navController = navController)
            }
            
            composable(Screen.Sales.route) {
                SalesScreen(navController = navController)
            }
            
            composable(Screen.CreateSale.route) {
                CreateSaleScreen(navController = navController)
            }
            
            composable(Screen.Reports.route) {
                ReportsScreen(navController = navController)
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            
            composable(Screen.ManageCategories.route) {
                // ManageCategoriesScreen(navController = navController)
            }
        }
    }
}