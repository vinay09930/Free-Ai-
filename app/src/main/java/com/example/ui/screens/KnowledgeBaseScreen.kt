package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Base", color = Color.White) },
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
                title = "Upload Document",
                icon = Icons.Default.UploadFile,
                onClick = {},
                accentColor = Color(0xFF10B981) // Green
            )
            GlassCard(
                title = "Search Collections",
                icon = Icons.Default.Search,
                onClick = {},
                accentColor = Color(0xFF3B82F6) // Blue
            )
            GlassCard(
                title = "My Collections",
                icon = Icons.Default.Folder,
                onClick = {},
                accentColor = Color(0xFFF59E0B) // Orange
            )
            Spacer(modifier = Modifier.weight(1f))
            Text("Indexed document summaries, RAG context vectors go here.", color = Color(0xFFA0A0A0), fontSize = 14.sp)
        }
    }
}
