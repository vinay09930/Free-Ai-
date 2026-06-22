package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIStudioScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Studio", color = Color.White) },
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
                title = "Prompt Editor",
                icon = Icons.Default.Tune,
                onClick = {},
                accentColor = Color(0xFFEAB308) // Yellow
            )
            GlassCard(
                title = "Compare Models",
                icon = Icons.Default.Compare,
                onClick = {},
                accentColor = Color(0xFF9333EA) // Purple
            )
            GlassCard(
                title = "Saved Presets",
                icon = Icons.Default.Save,
                onClick = {},
                accentColor = Color(0xFF3B82F6) // Blue
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("System prompts, temperature, and advanced configs go here.", color = Color(0xFFA0A0A0), fontSize = 14.sp)
        }
    }
}
