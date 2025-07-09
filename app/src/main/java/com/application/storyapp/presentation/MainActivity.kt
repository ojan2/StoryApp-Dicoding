package com.application.storyapp.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.application.storyapp.R
import com.application.storyapp.data.data_store.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.second_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        userPreferences = UserPreferences.getInstance(this)

        var keepSplashOn = true
        splashScreen.setKeepOnScreenCondition { keepSplashOn }

        lifecycleScope.launch {
            val isLoggedIn = userPreferences.isLoggedIn().first()

            // Get NavController
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            // Create new NavGraph with correct start destination
            val navGraph = navController.navInflater.inflate(R.navigation.navigation_menu).apply {
                // Set the proper start destination
                setStartDestination(
                    if (isLoggedIn) R.id.homeFragment else R.id.welcomeFragment
                )
            }

            // Apply the modified graph
            navController.graph = navGraph

            // After setting the graph, we can dismiss the splash
            delay(1500) // Minimum splash duration
            keepSplashOn = false
        }
    }
}