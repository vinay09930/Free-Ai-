package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.screens.ChatBubble
import com.example.ui.screens.ChatMessage
import com.example.ui.screens.ChatSkeletonBubble

@Composable
fun ChatInterface(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    textState: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onEditMessage: (String) -> Unit,
    leadingInputContent: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg, onEditMessage = { onEditMessage(it) })
            }
            if (isLoading) {
                item {
                    ChatSkeletonBubble()
                }
            }
        }

        // Chat Input Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f))
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                .windowInsetsPadding(WindowInsets.ime)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingInputContent()
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = textState,
                onValueChange = onTextChange,
                placeholder = { Text("Message FreeAI...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textState.isNotBlank() && !isLoading) {
                        onSendMessage()
                    }
                },
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                        RoundedCornerShape(50)
                    )
                    .size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}
