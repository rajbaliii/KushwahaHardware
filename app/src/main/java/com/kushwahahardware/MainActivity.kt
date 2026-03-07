package com.kushwahahardware

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kushwahahardware.security.LocalPermissionManager
import com.kushwahahardware.security.PermissionManager
import com.kushwahahardware.ui.MainScreen
import com.kushwahahardware.ui.screens.LoginScreen
import com.kushwahahardware.ui.theme.KushwahaHardwareTheme
import com.kushwahahardware.ui.viewmodel.SessionState
import com.kushwahahardware.ui.viewmodel.SessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            KushwahaHardwareTheme {
                CompositionLocalProvider(LocalPermissionManager provides permissionManager) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val sessionViewModel: SessionViewModel = hiltViewModel()
                        val sessionState by sessionViewModel.sessionState.collectAsState()

                        when (sessionState) {
                            is SessionState.LoggedIn -> {
                                MainScreen(onLogout = { sessionViewModel.logout() })
                            }
                            is SessionState.Loading -> { /* Splash */ }
                            else -> {
                                LoginScreen(viewModel = sessionViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
