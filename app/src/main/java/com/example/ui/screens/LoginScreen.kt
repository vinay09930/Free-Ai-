package com.example.ui.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.FreeAiApplication
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val firebaseManager = (context.applicationContext as FreeAiApplication).firebaseManager
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Use the fallback ID from strings.xml if google-services.json is missing
            .requestIdToken(context.getString(com.example.R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    coroutineScope.launch {
                        isLoading = true
                        val success = firebaseManager.signInWithGoogle(idToken)
                        if (success) {
                            account.email?.let { email ->
                                firebaseManager.saveUserProfile(account.displayName ?: "User", email)
                            }
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                Log.e("Auth", "Google sign in failed", e)
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (firebaseManager.isUserSignedIn()) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050208)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("FreeAI", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Text("The Unified AI Operating System", color = Color(0xFFA0A0A0), fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF9333EA))
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Continue with Google", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Notice for graceful degradation if they can't login due to missing google-services.json
            Surface(
                color = Color(0xFF9333EA).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "If testing without google-services.json, this acts as a mock UI.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
