package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) { // Dark Mode Only
        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize()) {
          val firebaseManager = (applicationContext as FreeAiApplication).firebaseManager
          val startDest = if (firebaseManager.isUserSignedIn()) "home" else "login"
          NavHost(navController = navController, startDestination = startDest) {
            composable("login") {
              com.example.ui.screens.LoginScreen(onLoginSuccess = {
                  navController.navigate("home") {
                      popUpTo("login") { inclusive = true }
                  }
              })
            }
            composable("home") {
              HomeScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToSettings = { navController.navigate("ai_studio") }
              )
            }
            composable("chat") {
              ChatScreen(
                onNavigateBack = { navController.popBackStack() }
              )
            }
            composable("ai_studio") {
               // AIStudioScreen()
            }
          }
        }
      }
    }
  }
}

