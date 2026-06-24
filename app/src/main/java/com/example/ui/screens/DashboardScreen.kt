package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfFile
import com.example.viewmodel.PdfViewModel

data class DashboardTool(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val category: String,
    val destinationRoute: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PdfViewModel,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPdfs by viewModel.allPdfs.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val tools = remember {
        listOf(
            DashboardTool("Chat with PDF", "Ask questions, extract content", Icons.Default.Chat, Color(0xFF6366F1), "AI", "ai_assistant"),
            DashboardTool("AI Summarizer", "Key takeaways & study notes", Icons.Default.AutoAwesome, Color(0xFF8B5CF6), "AI", "ai_assistant"),
            DashboardTool("Camera Scanner", "High-res scan with OCR", Icons.Default.PhotoCamera, Color(0xFFEC4899), "OCR", "scanner"),
            DashboardTool("Format Converter", "PDF to Word, Excel, JPG", Icons.Default.Transform, Color(0xFFF59E0B), "Convert", "scanner"),
            DashboardTool("Merge PDFs", "Combine multiple files", Icons.Default.CallMerge, Color(0xFF3B82F6), "Organize", "file_manager"),
            DashboardTool("Split PDFs", "Extract pages into new PDF", Icons.Default.CallSplit, Color(0xFF10B981), "Organize", "file_manager"),
            DashboardTool("Sign & Signatures", "Draw signature & watermark", Icons.Default.Draw, Color(0xFF14B8A6), "Edit", "file_manager"),
            DashboardTool("Draw & Write", "Highlights & freehand annotations", Icons.Default.Edit, Color(0xFF06B6D4), "Edit", "file_manager"),
            DashboardTool("Lock & Protect", "AES-256 password lock", Icons.Default.Lock, Color(0xFFEF4444), "Security", "settings"),
            DashboardTool("Cloud Sync", "Backup to Google Drive", Icons.Default.CloudQueue, Color(0xFF00C6FF), "Cloud", "settings")
        )
    }

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val filteredTools = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") tools
        else tools.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "PDF Tech masters",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Professional Document Hub",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToRoute("premium") },
                        modifier = Modifier.testTag("premium_gift_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardMembership,
                            contentDescription = "Membership Hub",
                            tint = if (isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToRoute("settings") },
                        modifier = Modifier.testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Promo Card
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -20 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4F46E5),
                                        Color(0xFF9333EA)
                                    )
                                )
                            )
                            .clickable {
                                if (isPremium) {
                                    onNavigateToRoute("ai_assistant")
                                } else {
                                    onNavigateToRoute("premium")
                                }
                            }
                            .testTag("promo_hero_card"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(0.68f)
                        ) {
                            Badge(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    if (isPremium) "PREMIUM ACTIVE" else "UNLOCK AI POWER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                if (isPremium) "Analyze with Gemini" else "Chat & Summarize PDFs",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (isPremium) "Your smart academic & business document companion is ready." else "Ask anything, run OCR, and summarize in seconds with Gemini.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(130.dp)
                                .offset(x = 10.dp)
                        )
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "My PDFs",
                        value = "${allPdfs.size} files",
                        icon = Icons.Default.FolderOpen,
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Security",
                        value = "${allPdfs.count { it.isLocked }} locked",
                        icon = Icons.Default.Security,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Categories Filter Chips Row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val categories = listOf("All", "AI", "Edit", "OCR", "Convert")
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_$cat")
                        )
                    }
                }
            }

            // Grid of PDF Tools
            item {
                Column {
                    Text(
                        "Document Tools",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Compose Grid alternative to avoid nested scroll issues
                    val chunkedTools = filteredTools.chunked(2)
                    chunkedTools.forEach { rowTools ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            rowTools.forEach { tool ->
                                ToolCard(
                                    tool = tool,
                                    onClick = {
                                        if (tool.destinationRoute == "ai_assistant" && viewModel.selectedPdf.value == null) {
                                            // Auto-select first PDF for user if none selected
                                            if (allPdfs.isNotEmpty()) {
                                                viewModel.selectPdf(allPdfs.first())
                                            }
                                        }
                                        onNavigateToRoute(tool.destinationRoute)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTools.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Recents Preview Section
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        "Recent Documents",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { onNavigateToRoute("file_manager") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("See All", fontSize = 13.sp)
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (allPdfs.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No documents found",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(allPdfs.take(3)) { pdf ->
                    RecentPdfItem(
                        pdf = pdf,
                        onClick = {
                            viewModel.selectPdf(pdf)
                            onNavigateToRoute("pdf_editor")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: DashboardTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() }
            .testTag("tool_${tool.title.replace(" ", "_").lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tool.color.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = tool.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tool.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun RecentPdfItem(
    pdf: PdfFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("recent_pdf_${pdf.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (pdf.isLocked) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
            ) {
                Icon(
                    imageVector = if (pdf.isLocked) Icons.Default.Lock else Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = if (pdf.isLocked) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = pdf.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pdf.pageCount} pages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                    Text(
                        text = "%.1f MB".format(pdf.size / 1024000.0),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pdf.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Starred",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
