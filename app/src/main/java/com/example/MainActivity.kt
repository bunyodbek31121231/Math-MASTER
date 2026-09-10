package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.MathMasterTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)

        setContent {
            val themeMode by appContainer.userPreferences.themeMode.collectAsState(initial = "SYSTEM")
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MathMasterTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    container = appContainer
                )
            }
        }
    }
}
