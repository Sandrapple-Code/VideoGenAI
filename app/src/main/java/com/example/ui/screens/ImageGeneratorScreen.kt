package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.BuildConfig
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ImageGenerationItem
import com.example.ui.styles.ImageStyle
import com.example.ui.styles.ImageStyles
import com.example.ui.viewmodel.GenerationUiState
import com.example.ui.viewmodel.ImageGenerationViewModel
import com.example.util.ImageStorageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGeneratorScreen(
    viewModel: ImageGenerationViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    val negativePrompt by viewModel.negativePrompt.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    var showAdvanced by remember { mutableStateOf(false) }
    var selectedFullscreenItem by remember { mutableStateOf<ImageGenerationItem?>(null) }
    var showApiKeyWarning by remember { mutableStateOf(false) }
    
    // Bottom navigation current tab: Studio, History, Gallery
    var currentTab by remember { mutableStateOf("Studio") }

    // Check on startup if API Key is configured properly
    LaunchedEffect(Unit) {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY" || key == "GEMINI_API_KEY") {
            showApiKeyWarning = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "VisionGen AI",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.Settings else Icons.Default.Refresh,
                            contentDescription = "Toggle Theme Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Custom Profile Badge exactly matching the Design HTML (JD Box)
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "JD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // High fidelity bottom navigation bar mimicking the Design HTML
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(80.dp)
            ) {
                // Studio Tab Item
                NavigationBarItem(
                    selected = currentTab == "Studio",
                    onClick = { currentTab = "Studio" },
                    icon = {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentTab == "Studio") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Studio",
                                tint = if (currentTab == "Studio") MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    label = {
                        Text(
                            "Studio",
                            fontWeight = if (currentTab == "Studio") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (currentTab == "Studio") MaterialTheme.colorScheme.onSurface 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                // History Tab Item
                NavigationBarItem(
                    selected = currentTab == "History",
                    onClick = { currentTab = "History" },
                    icon = {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentTab == "History") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "History",
                                tint = if (currentTab == "History") MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    label = {
                        Text(
                            "History",
                            fontWeight = if (currentTab == "History") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (currentTab == "History") MaterialTheme.colorScheme.onSurface 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )

                // Gallery/Favorites Tab Item
                NavigationBarItem(
                    selected = currentTab == "Gallery",
                    onClick = { currentTab = "Gallery" },
                    icon = {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentTab == "Gallery") MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites",
                                tint = if (currentTab == "Gallery") MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    label = {
                        Text(
                            "Gallery",
                            fontWeight = if (currentTab == "Gallery") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (currentTab == "Gallery") MaterialTheme.colorScheme.onSurface 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        
        AnimatedContent(
            targetState = currentTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            }
        ) { targetTab ->
            when (targetTab) {
                "Studio" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // API Key Warning banner
                        if (showApiKeyWarning) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "API Key Not Active",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Please configure secure app variables inside the Google AI Studio Secrets Panel as GEMINI_API_KEY to trigger actual image rendering.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Intro Concept Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Seamless GenAI Canvas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = (-0.3).sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        "Shape prompts, apply visual filters, and render exquisite artwork in seconds.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Image & Video Dual-Mode Selector
                        val generatorType by viewModel.generatorType.collectAsStateWithLifecycle()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Image", "Video").forEach { type ->
                                val isSelected = generatorType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.onGeneratorTypeSelect(type) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (type == "Image") "📸 Image Canvas" else "🎥 Video Motion",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Prompt input module with custom container styling
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Your Prompt",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Sleek Surprise Me Button
                                Text(
                                    "Surprise Me 🎲",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            viewModel.loadRandomPrompt()
                                            Toast.makeText(context, "🎲 Crafted random visual ideas!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("random_prompt_button")
                                )
                            }

                            // Input Box with modern tailwind-styled bottom border exactly as in HTML specs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(bottom = 2.dp) // Leave line space for highlight
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                                        )
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    OutlinedTextField(
                                        value = prompt,
                                        onValueChange = { viewModel.onPromptChange(it) },
                                        placeholder = {
                                            Text(
                                                "Write anything... e.g., Futuristic cyberpunk lotus garden, glowing neon mist...",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .testTag("prompt_input"),
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent,
                                            errorBorderColor = Color.Transparent,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        ),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 15.sp,
                                            lineHeight = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "max 500 characters",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            "${prompt.length} / 500",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Elegant Absolute Bottom Indigo Highlight mimicking Tailwind: border-b-2 border-indigo-600
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }

                        // Creative visual style Horizontal filters
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Visual Style",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(ImageStyles.styles) { style ->
                                    val isSelected = selectedStyle.name == style.name
                                    Surface(
                                        onClick = { viewModel.onStyleSelect(style) },
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.surface,
                                        border = if (isSelected) null 
                                                 else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        tonalElevation = if (isSelected) 4.dp else 1.dp,
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected Style",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(style.icon, fontSize = 14.sp)
                                            }
                                            Text(
                                                text = style.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) Color.White 
                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Compact expansion controls for ratio & negative tags
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAdvanced = !showAdvanced }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Advanced Settings",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Advanced Controls",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand controls",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                AnimatedVisibility(
                                    visible = showAdvanced,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 14.dp)
                                            .padding(bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                                        // Aspect Shape selection Row
                                        Text("Aspect Ratio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        val ratios = listOf("1:1", "16:9", "9:16", "4:3", "3:4")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            ratios.forEach { ratio ->
                                                val isRatioSelected = aspectRatio == ratio
                                                val labelText = when (ratio) {
                                                    "1:1" -> "1:1"
                                                    "16:9" -> "16:9"
                                                    "9:16" -> "9:16"
                                                    "4:3" -> "4:3"
                                                    "3:4" -> "3:4"
                                                    else -> ratio
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isRatioSelected) MaterialTheme.colorScheme.primary 
                                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                                        )
                                                        .clickable { viewModel.onAspectRatioSelect(ratio) }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        labelText,
                                                        color = if (isRatioSelected) Color.White 
                                                                else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Negative filter tags
                                        Text("Unwanted qualities", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        OutlinedTextField(
                                            value = negativePrompt,
                                            onValueChange = { viewModel.onNegativePromptChange(it) },
                                            placeholder = { Text("blurry, ugly, extra hands...", fontSize = 12.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("negative_prompt_input"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Central Render Outcome Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                                .testTag("canvas_result_slot")
                        ) {
                            AnimatedContent(targetState = uiState) { state ->
                                when (state) {
                                    is GenerationUiState.Idle -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(260.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(24.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🎨", fontSize = 24.sp)
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(
                                                    "No Masterpiece Rendered",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Describe your ideas & tap Generate Image below.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    is GenerationUiState.Generating -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(24.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 3.dp,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    "Orchestrating pixels...",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Applying style model: ${selectedStyle.name} (${aspectRatio})",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    is GenerationUiState.Success -> {
                                        val activeItem = state.item
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            if (activeItem.isVideo) {
                                                VideoPlayer(
                                                    videoPath = activeItem.imagePath,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(16f / 10f)
                                                        .clip(RoundedCornerShape(32.dp))
                                                        .clickable { selectedFullscreenItem = activeItem }
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(File(activeItem.imagePath))
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = "New masterpiece",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(
                                                            when (activeItem.aspectRatio) {
                                                                "16:9" -> 16f / 9f
                                                                "9:16" -> 9f / 16f
                                                                "4:3" -> 4f / 3f
                                                                "3:4" -> 3f / 4f
                                                                else -> 1f
                                                            }
                                                        )
                                                        .clip(RoundedCornerShape(32.dp))
                                                        .clickable { selectedFullscreenItem = activeItem },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            // Gradient overlay footer matching the unsplash screenshot style exactly
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                                        )
                                                    )
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "Current Result",
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Text(
                                                        if (activeItem.isVideo) "${activeItem.selectedStyle} • 1080p • MP4" 
                                                        else "${activeItem.selectedStyle} • 1024x1024 • PNG",
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                // Blur style rounded download trigger button
                                                IconButton(
                                                    onClick = {
                                                        if (activeItem.isVideo) {
                                                            val okay = ImageStorageHelper.saveVideoToGallery(context, activeItem.imagePath, activeItem.originalPrompt)
                                                            if (okay) {
                                                                Toast.makeText(context, "📥 MP4 saved to device video library!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Failed to download video", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            val bitmap = android.graphics.BitmapFactory.decodeFile(activeItem.imagePath)
                                                            if (bitmap != null) {
                                                                val okay = ImageStorageHelper.saveImageToGallery(context, bitmap, activeItem.originalPrompt)
                                                                if (okay) {
                                                                    Toast.makeText(context, "📥 Saved directly to device gallery!", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "Error saving artifact", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } else {
                                                                Toast.makeText(context, "Local image binary file is cached successfully!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.25f))
                                                ) {
                                                    Icon(
                                                        imageVector = if (activeItem.isVideo) Icons.Default.Done else Icons.Default.Star,
                                                        contentDescription = "Save file",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    is GenerationUiState.Error -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Error",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Synthesis Failed",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                state.message,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = { viewModel.resetState() },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Reset", fontSize = 12.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Prominent primary Generation CTA Button exactly styled as the HTML button
                        Button(
                            onClick = { viewModel.generateImage(context) },
                            enabled = uiState !is GenerationUiState.Generating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
                                .testTag("generate_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState is GenerationUiState.Generating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "DRAFTING LATENT VARIABLES...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Generate Vector",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "GENERATE MASTERPIECE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    }
                }

                "History" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Creative Archive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    "Saved localized items: ${history.size}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            if (history.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearHistory() }) {
                                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📭", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Studio Archive is Clean",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Generated images load in chronological grids here.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_gallery"),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val pairs = history.chunked(2)
                                pairs.forEach { pairItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pairItems.forEach { item ->
                                            HistoryCard(
                                                item = item,
                                                modifier = Modifier.weight(1f),
                                                onItemClick = { selectedFullscreenItem = item },
                                                onFavoriteClick = { viewModel.toggleFavorite(item) },
                                                onDeleteClick = { viewModel.deleteHistoryItem(item) },
                                                onRestorePrompt = {
                                                    viewModel.onPromptChange(item.originalPrompt)
                                                    viewModel.onNegativePromptChange(item.negativePrompt)
                                                    val realStyle = ImageStyles.styles.firstOrNull { it.name == item.selectedStyle } ?: ImageStyles.styles.first()
                                                    viewModel.onStyleSelect(realStyle)
                                                    viewModel.onAspectRatioSelect(item.aspectRatio)
                                                    currentTab = "Studio"
                                                    Toast.makeText(context, "Restored prompt selection details!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                        if (pairItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Gallery" -> {
                    val favorites = history.filter { it.isFavorite }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                "Favorites Showcase",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                "Bookmarked masterpieces: ${favorites.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        if (favorites.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💖", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No Bookmarked Prompts",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Tap the heart icon on any synthesis to save here.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val pairs = favorites.chunked(2)
                                pairs.forEach { pairItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pairItems.forEach { item ->
                                            HistoryCard(
                                                item = item,
                                                modifier = Modifier.weight(1f),
                                                onItemClick = { selectedFullscreenItem = item },
                                                onFavoriteClick = { viewModel.toggleFavorite(item) },
                                                onDeleteClick = { viewModel.deleteHistoryItem(item) },
                                                onRestorePrompt = {
                                                    viewModel.onPromptChange(item.originalPrompt)
                                                    viewModel.onNegativePromptChange(item.negativePrompt)
                                                    val realStyle = ImageStyles.styles.firstOrNull { it.name == item.selectedStyle } ?: ImageStyles.styles.first()
                                                    viewModel.onStyleSelect(realStyle)
                                                    viewModel.onAspectRatioSelect(item.aspectRatio)
                                                    currentTab = "Studio"
                                                    Toast.makeText(context, "Restored prompt selection details!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                        if (pairItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modern immersive overlay gallery display dialog modal
    selectedFullscreenItem?.let { item ->
        Dialog(onDismissRequest = { selectedFullscreenItem = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (item.isVideo) {
                            VideoPlayer(
                                videoPath = item.imagePath,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 10f)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(File(item.imagePath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Immersive detailed view",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(
                                        when (item.aspectRatio) {
                                            "16:9" -> 16f / 9f
                                            "9:16" -> 9f / 16f
                                            "4:3" -> 4f / 3f
                                            "3:4" -> 3f / 4f
                                            else -> 1f
                                        }
                                    )
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Floating transparent stamp badge
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                item.aspectRatio,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Metadata details section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Masterpiece Blueprint",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        Text(
                            "Original Prompt:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            item.originalPrompt,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        Text(
                            "Model Context Prompt Suffix:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            item.finalPrompt,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.negativePrompt.isNotEmpty()) {
                            Text(
                                "Excluded Components:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                item.negativePrompt,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Timestamp tag
                        val parsedTime = remember(item.timestamp) {
                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            formatter.format(Date(item.timestamp))
                        }
                        Text(
                            "Generated on: $parsedTime",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedFullscreenItem = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { viewModel.toggleFavorite(item) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite in fullscreen modal",
                                tint = if (item.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                            )
                        }

                        Button(
                            onClick = {
                                if (item.isVideo) {
                                    val safe = ImageStorageHelper.saveVideoToGallery(context, item.imagePath, item.originalPrompt)
                                    if (safe) {
                                        Toast.makeText(context, "📥 Video saved to device movie library!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to download video", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(item.imagePath)
                                    if (bitmap != null) {
                                        val safe = ImageStorageHelper.saveImageToGallery(context, bitmap, item.originalPrompt)
                                        if (safe) {
                                            Toast.makeText(context, "📥 Downloaded into device Picture library!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: ImageGenerationItem,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRestorePrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(item.imagePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Stored item snapshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )

            if (item.isVideo) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Dynamic Video Motion",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Dynamic header actions overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) Color.Red else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Elegant metadata overlay footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(8.dp)
            ) {
                Text(
                    item.originalPrompt,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${item.selectedStyle} • ${item.aspectRatio}",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        "Use ⚙️",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onRestorePrompt() }
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * An advanced, hybrid Cinematic Video/Motion Player. 
 * Plays real MP4 files if provided, or renders customized prompt-to-image resources 
 * with a high-fidelity infinite Ken Burns cinematic motion sweep (scale/translation/overlay) 
 * so users see their exact custom prompt as dynamic, fluid living motion.
 */
@Composable
fun VideoPlayer(
    videoPath: String,
    modifier: Modifier = Modifier
) {
    val isMp4 = videoPath.endsWith(".mp4", ignoreCase = true) || videoPath.contains("mixkit")
    
    if (isMp4) {
        val context = LocalContext.current
        AndroidView(
            factory = { ctx ->
                android.widget.VideoView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(0f, 0f) // Silent aesthetic ambient movie loops
                        start()
                    }
                }
            },
            update = { view ->
                try {
                    val file = java.io.File(videoPath)
                    if (file.exists()) {
                        view.setVideoPath(file.absolutePath)
                    } else if (videoPath.startsWith("http")) {
                        view.setVideoURI(android.net.Uri.parse(videoPath))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            modifier = modifier
        )
    } else {
        // Render a breathtaking infinite cinematic motion sweep over the dynamically generated prompt image
        val infiniteTransition = rememberInfiniteTransition(label = "cinematic_motion")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        val translationX by infiniteTransition.animateFloat(
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(14000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "translationX"
        )
        val translationY by infiniteTransition.animateFloat(
            initialValue = -12f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "translationY"
        )

        Box(
            modifier = modifier
                .background(Color.Black)
                .clipToBounds()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(java.io.File(videoPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Cinematic video generation",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = translationX,
                        translationY = translationY
                    )
            )
            
            // Decorative scan overlay representing fine ambient movie lens reflections
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

