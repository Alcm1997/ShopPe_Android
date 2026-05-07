package com.idat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.presentation.navigation.AppNavigation
import com.idat.presentation.ui.theme.ShopPeUITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos el Splash Screen
        val splashScreen = installSplashScreen()
        
        // Mantenemos el Splash visible por 2 segundos para mostrar el logo
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }
        
        // Quitamos el splash después de 2 segundos
        window.decorView.postDelayed({
            keepSplash = false
        }, 2000)

        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by userPreferencesManager.isDarkTheme.collectAsState(
                initial = isSystemInDarkTheme()
            )

            ShopPeUITheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
