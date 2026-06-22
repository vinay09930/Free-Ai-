package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHubScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Hub", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF050208)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(
                title = "Official APIs",
                icon = Icons.Default.Cloud,
                onClick = {},
                accentColor = Color(0xFF3B82F6) // Blue
            )
            GlassCard(
                title = "Browser Providers",
                icon = Icons.Default.Language,
                onClick = {},
                accentColor = Color(0xFF10B981) // Green
            )
            GlassCard(
                title = "Local Engines",
                icon = Icons.Default.Computer,
                onClick = {},
                accentColor = Color(0xFFF59E0B) // Orange
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("API Keys and Provider health logs go here.", color = Color(0xFFA0A0A0), fontSize = 14.sp)
        }
    }
}
