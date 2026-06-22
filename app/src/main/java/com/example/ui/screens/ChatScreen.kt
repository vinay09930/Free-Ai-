package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    var textState by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            if (spokenText != null) {
                textState += if (textState.isBlank()) spokenText else " $spokenText"
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    val content = messages.joinToString("\n\n") { msg ->
                        "${if (msg.isUser) "User" else "FreeAI"}:\n${msg.text}"
                    }
                    out.write(content.toByteArray())
                }
                Toast.makeText(context, "Export saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    val systemPrompt by viewModel.systemPrompt.collectAsState()

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem == null || lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(messages.size, isLoading) {
        if (isAtBottom && (messages.isNotEmpty() || isLoading)) {
            listState.animateScrollToItem(if (isLoading) messages.size else messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F0C29),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Sessions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                Button(
                    onClick = { viewModel.createNewSession(); scope.launch { drawerState.close() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
                ) {
                    Text("+ New Chat")
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search sessions...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF9333EA),
                        unfocusedBorderColor = Color(0xFFFFFFFF).copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF22D3EE)
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                val filteredSessions = sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }

                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    items(filteredSessions) { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.loadSession(session.id)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFFA0A0A0))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(session.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF050208),
            topBar = {
                TopAppBar(
                    title = { 
                        var expanded by remember { mutableStateOf(false) }
                        val currentModel by viewModel.selectedModel.collectAsState()
                        val availableModels by viewModel.availableModels.collectAsState()
                        
                        Box {
                            Column(modifier = Modifier.clickable { expanded = true }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("FreeAI Chat", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Model", tint = Color.White)
                                }
                                Text("Using $currentModel", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF0F0C29))
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model, color = Color.White) },
                                        onClick = { 
                                            viewModel.selectModel(model)
                                            expanded = false 
                                        }
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = { saveFileLauncher.launch("FreeAI_Chat.txt") }) {
                            Icon(Icons.Default.Share, contentDescription = "Export Chat", tint = Color.White)
                        }
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
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
                                colors = listOf(Color(0xFF9333EA).copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )

                com.example.ui.components.ChatInterface(
                    messages = messages,
                    isLoading = isLoading,
                    listState = listState,
                    textState = textState,
                    onTextChange = { textState = it },
                    onSendMessage = {
                        viewModel.sendMessage(textState)
                        textState = ""
                    },
                    onEditMessage = { textState = it },
                    leadingInputContent = {
                        IconButton(
                            onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Speech to Text",
                                tint = Color.White
                            )
                        }
                    }
                )

                if (showSettingsDialog) {
                    var editPrompt by remember { mutableStateOf(systemPrompt) }
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        containerColor = Color(0xFF1E1E2E),
                        title = { Text("Chat Settings", color = Color.White) },
                        text = {
                            Column {
                                Text("System Prompt", color = Color(0xFFA0A0A0), fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editPrompt,
                                    onValueChange = { editPrompt = it },
                                    placeholder = { Text("Define the persona or behavior...") },
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color(0xFF9333EA),
                                        focusedBorderColor = Color(0xFF9333EA)
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { 
                                viewModel.setSystemPrompt(editPrompt)
                                showSettingsDialog = false 
                            }) {
                                Text("Save", color = Color(0xFF4ADE80))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Cancel", color = Color(0xFFF87171))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onEditMessage: ((String) -> Unit)? = null) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        if (message.isUser) {
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp))
                        .background(Color(0xFF9333EA).copy(alpha = 0.8f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Status indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(message.text))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    }
                    if (onEditMessage != null) {
                        IconButton(onClick = { onEditMessage(message.text) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    when (message.status) {
                        MessageStatus.SENDING -> {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color(0xFF94A3B8), strokeWidth = 1.dp)
                        }
                        MessageStatus.DELIVERED -> {
                            Text("Delivered", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Check, contentDescription = "Delivered", tint = Color(0xFF4ADE80), modifier = Modifier.size(12.dp))
                        }
                        MessageStatus.ERROR -> {
                            Text("Error", color = Color.Red, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.Start) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                        .background(Color(0xFFFFFFFF).copy(alpha = 0.05f))
                        .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                        .padding(12.dp)
                ) {
                    MarkdownText(message.text)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy to Clipboard", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownText(text: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val parts = text.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (part.isBlank()) return@forEachIndexed
            if (index % 2 == 0) {
                // Regular markdown text
                Text(
                    text = part.trim(),
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            } else {
                // Code block
                val lines = part.trim().lines()
                val language = lines.firstOrNull() ?: ""
                val code = if (lines.size > 1) lines.drop(1).joinToString("\n") else lines.joinToString("\n")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2D2D2D))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language.ifBlank { "code" },
                                color = Color(0xFFA0A0A0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { 
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color(0xFFA0A0A0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Text(
                                text = com.example.ui.components.CodeSyntaxHighlighter.highlightCode(code, language),
                                color = Color(0xFFE5E5E5),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSkeletonBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    
    // We create three separate animated values for bouncing dots
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 0 with LinearEasing
                -10.0f at 300 with FastOutSlowInEasing
                0.0f at 600 with FastOutSlowInEasing
                0.0f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 0 with LinearEasing
                0.0f at 150 with LinearEasing
                -10.0f at 450 with FastOutSlowInEasing
                0.0f at 750 with FastOutSlowInEasing
                0.0f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.0f at 0 with LinearEasing
                0.0f at 300 with LinearEasing
                -10.0f at 600 with FastOutSlowInEasing
                0.0f at 900 with FastOutSlowInEasing
                0.0f at 1200 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 60.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                .background(Color(0xFFFFFFFF).copy(alpha = 0.05f))
                .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.offset(y = dot1Offset.dp).size(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.7f)))
                Box(modifier = Modifier.offset(y = dot2Offset.dp).size(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.7f)))
                Box(modifier = Modifier.offset(y = dot3Offset.dp).size(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.7f)))
            }
        }
    }
}
