package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToAIStudio: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF050208)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = (-48).dp, y = (-48).dp)
                    .size(250.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF9333EA).copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 48.dp, y = 48.dp)
                    .size(250.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "OS v2.4.0",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22D3EE),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "FreeAI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Your Local & Connected AI OS",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Animated usage stat placeholder
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .clickable { onNavigateToProviders() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "SYSTEM STATUS (TAP TO VIEW PROVIDERS)",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF4ADE80))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Ready", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF9333EA).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("0 SESSIONS", color = Color(0xFFD8B4FE), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassCard(
                            title = "Talk to FreeAI",
                            icon = Icons.AutoMirrored.Filled.Chat,
                            onClick = onNavigateToChat,
                            accentColor = Color(0xFF9333EA)
                        )
                    }
                    item {
                        GlassCard(
                            title = "Local Models",
                            icon = Icons.Default.Storage,
                            onClick = onNavigateToModels,
                            accentColor = Color(0xFF06B6D4)
                        )
                    }
                    item {
                        GlassCard(
                            title = "AI Studio",
                            icon = Icons.Default.Settings,
                            onClick = onNavigateToAIStudio,
                            accentColor = Color(0xFFF59E0B)
                        )
                    }
                    item {
                        GlassCard(
                            title = "Knowledge Base",
                            icon = Icons.Default.AccountCircle,
                            onClick = onNavigateToKnowledgeBase,
                            accentColor = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFFFFFFF).copy(alpha = 0.03f))
            .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(32.dp))
    ) {
        content()
    }
}
