package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProviderHubScreen
import com.example.ui.screens.ModelHubScreen
import com.example.ui.screens.KnowledgeBaseScreen
import com.example.ui.screens.AIStudioScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      var isDarkTheme by remember { mutableStateOf(true) }
      MyApplicationTheme(darkTheme = isDarkTheme) { 
        val navController = rememberNavController()
        Surface(modifier = Modifier.fillMaxSize()) {
          val firebaseManager = (applicationContext as FreeAiApplication).firebaseManager
          val startDest = if (firebaseManager.isUserSignedIn()) "home" else "login"
          NavHost(navController = navController, startDestination = startDest) {
            composable("login") {
              LoginScreen(onLoginSuccess = {
                  navController.navigate("home") {
                      popUpTo("login") { inclusive = true }
                  }
              })
            }
            composable("home") {
              HomeScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToAIStudio = { navController.navigate("ai_studio") },
                onNavigateToProviders = { navController.navigate("providers") },
                onNavigateToModels = { navController.navigate("models") },
                onNavigateToKnowledgeBase = { navController.navigate("knowledge_base") }
              )
            }
            composable("chat") {
              ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme }
              )
            }
            composable("ai_studio") {
               AIStudioScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("providers") {
               ProviderHubScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("models") {
               ModelHubScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("knowledge_base") {
               KnowledgeBaseScreen(onNavigateBack = { navController.popBackStack() })
            }
          }
        }
      }
    }
  }
}


