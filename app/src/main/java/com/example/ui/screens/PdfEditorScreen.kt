package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfFile
import com.example.viewmodel.DrawingStroke
import com.example.viewmodel.PdfPage
import com.example.viewmodel.PdfViewModel
import com.example.viewmodel.TextOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfEditorScreen(
    viewModel: PdfViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPdf by viewModel.selectedPdf.collectAsState()
    val pages by viewModel.pdfPages.collectAsState()
    val currentPageNo by viewModel.selectedPageNumber.collectAsState()

    val currentPage = pages.find { it.pageNumber == currentPageNo }

    var editorMode by remember { mutableStateOf("View") } // "View", "Draw", "Text", "Annotate", "Redact"

    // Text Overlay inputs
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showWatermarkDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var textX by remember { mutableFloatStateOf(0.5f) }
    var textY by remember { mutableFloatStateOf(0.5f) }
    var textIsHeaderFooter by remember { mutableStateOf(false) }

    // Finger Sketch State
    var currentPoints = remember { mutableStateListOf<Pair<Float, Float>>() }

    // Color picker
    var selectedColor by remember { mutableStateOf(Color.Red) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            selectedPdf?.name ?: "Editor",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        selectedPdf?.let {
                            Text(
                                "Page $currentPageNo of ${pages.size}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.rotateCurrentPage() },
                        modifier = Modifier.testTag("editor_rotate_button")
                    ) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90°")
                    }
                    IconButton(
                        onClick = { viewModel.duplicateCurrentPage() },
                        modifier = Modifier.testTag("editor_duplicate_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Page")
                    }
                    IconButton(
                        onClick = { viewModel.deleteCurrentPage() },
                        enabled = pages.size > 1,
                        modifier = Modifier.testTag("editor_delete_page_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Delete Page")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Interactive Editing Toolbar
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_toolbar")
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    ToolbarButton(
                        label = "Pan",
                        icon = Icons.Default.PanTool,
                        active = editorMode == "View",
                        onClick = { editorMode = "View" },
                        modifier = Modifier.testTag("mode_view")
                    )
                    ToolbarButton(
                        label = "Draw / Sign",
                        icon = Icons.Default.Draw,
                        active = editorMode == "Draw",
                        onClick = { editorMode = "Draw" },
                        modifier = Modifier.testTag("mode_draw")
                    )
                    ToolbarButton(
                        label = "Text",
                        icon = Icons.Default.TextFields,
                        active = editorMode == "Text",
                        onClick = {
                            editorMode = "Text"
                            showAddTextDialog = true
                        },
                        modifier = Modifier.testTag("mode_text")
                    )
                    ToolbarButton(
                        label = "Watermark",
                        icon = Icons.Default.BrandingWatermark,
                        active = editorMode == "Annotate",
                        onClick = {
                            showWatermarkDialog = true
                        },
                        modifier = Modifier.testTag("mode_watermark")
                    )
                    ToolbarButton(
                        label = "Redact",
                        icon = Icons.Default.VisibilityOff,
                        active = editorMode == "Redact",
                        onClick = {
                            viewModel.redactCurrentPage()
                        },
                        modifier = Modifier.testTag("mode_redact")
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (selectedPdf == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        "No document loaded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        "Please select or import a PDF from the Workspace to begin editing.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Open Workspace")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                // Horizontal Mini Page Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                ) {
                    items(pages) { p ->
                        val isSelected = p.pageNumber == currentPageNo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 50.dp, height = 70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectPage(p.pageNumber) }
                                .testTag("page_carousel_${p.pageNumber}")
                        ) {
                            Text(
                                "P. ${p.pageNumber}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Drawing Canvas tools overlay
                if (editorMode == "Draw") {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val colors = listOf(Color.Red, Color.Blue, Color.Black, Color.Green)
                            colors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(
                                            width = if (selectedColor == c) 2.dp else 0.dp,
                                            color = if (selectedColor == c) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = c }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (currentPoints.isNotEmpty()) {
                                    viewModel.drawStrokeOnCurrentPage(
                                        DrawingStroke(
                                            points = currentPoints.toList(),
                                            color = selectedColor.toArgb()
                                        )
                                    )
                                    currentPoints.clear()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Save Drawing", fontSize = 11.sp)
                        }
                    }
                }

                // Main Interactive PDF Canvas Sheet
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    currentPage?.let { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f / 1.414f) // Standard A4 Aspect Ratio
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .rotate(page.rotation.toFloat())
                                .pointerInput(editorMode) {
                                    if (editorMode == "Draw") {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                currentPoints.add(offset.x to offset.y)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                currentPoints.add(change.position.x to change.position.y)
                                            },
                                            onDragEnd = {
                                                // Temporarily keep them till manual save or save instantly
                                            }
                                        )
                                    }
                                }
                                .testTag("main_pdf_sheet"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render page decorations and layers
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // 1. Render all highlights/annotations
                                page.annotations.forEach { ann ->
                                    val rColor = Color(ann.color).copy(alpha = 0.4f)
                                    drawRect(
                                        color = rColor,
                                        topLeft = androidx.compose.ui.geometry.Offset(
                                            ann.xStartPercent * size.width,
                                            ann.yStartPercent * size.height
                                        ),
                                        size = androidx.compose.ui.geometry.Size(
                                            (ann.xEndPercent - ann.xStartPercent) * size.width,
                                            (ann.yEndPercent - ann.yStartPercent) * size.height
                                        )
                                    )
                                }

                                // 2. Draw saved sketch strokes
                                page.drawings.forEach { stroke ->
                                    val path = Path()
                                    stroke.points.forEachIndexed { idx, pt ->
                                        if (idx == 0) path.moveTo(pt.first, pt.second)
                                        else path.lineTo(pt.first, pt.second)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(stroke.color),
                                        style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                // 3. Draw active unsaved brush stroke
                                if (currentPoints.isNotEmpty()) {
                                    val path = Path()
                                    currentPoints.forEachIndexed { idx, pt ->
                                        if (idx == 0) path.moveTo(pt.first, pt.second)
                                        else path.lineTo(pt.first, pt.second)
                                    }
                                    drawPath(
                                        path = path,
                                        color = selectedColor,
                                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // 4. Render text overlays (watermarks, headers, etc)
                            page.textOverlays.forEach { textOverlay ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = textOverlay.text,
                                        fontWeight = FontWeight.Bold,
                                        color = if (textOverlay.isWatermark) Color.LightGray.copy(alpha = 0.4f) else Color.DarkGray,
                                        fontSize = if (textOverlay.isWatermark) 32.sp else 12.sp,
                                        modifier = Modifier
                                            .align(
                                                if (textOverlay.isHeaderFooter) {
                                                    if (textOverlay.yPercent < 0.2f) Alignment.TopCenter else Alignment.BottomCenter
                                                } else Alignment.Center
                                            )
                                            .rotate(if (textOverlay.isWatermark) -45f else 0f)
                                    )
                                }
                            }

                            // Default base visual page content text
                            if (!page.isRedacted) {
                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "SECTION ${page.pageNumber}: ANALYSIS REPORT SUMMARY",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    
                                    Column {
                                        Text(
                                            "A robust offline document architecture utilizing Kotlin DSL and Room databases ensures complete security over corporate assets. The dynamic theme rendering adapts across dark mode and high-resolution tablet matrices.",
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = Color.Black.copy(0.75f)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            "This sandbox provides dynamic PDF modification, page rotation, splitting, and signature integration with Gemini context engines.",
                                            fontSize = 11.sp,
                                            color = Color.Black.copy(0.55f)
                                        )
                                    }

                                    Text(
                                        "PDF Tech masters Hub - Page ${page.pageNumber} of ${pages.size}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // REDACTION FULL OVERLAY
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "CONTENT REDACTED",
                                        color = Color.Red,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- INTERACTIVE DIALOGS ---

        // Add Text dialog
        if (showAddTextDialog) {
            AlertDialog(
                onDismissRequest = { showAddTextDialog = false },
                title = { Text("Add Text Overlay") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("Enter Text") },
                            modifier = Modifier.fillMaxWidth().testTag("overlay_text_field")
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = textIsHeaderFooter,
                                onCheckedChange = { textIsHeaderFooter = it }
                            )
                            Text("Set as Header / Footer style", fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (textInput.isNotEmpty()) {
                                viewModel.addTextOverlayToCurrentPage(
                                    text = textInput,
                                    x = 0.5f,
                                    y = if (textIsHeaderFooter) 0.05f else 0.5f,
                                    isHeader = textIsHeaderFooter
                                )
                                textInput = ""
                                textIsHeaderFooter = false
                                showAddTextDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_overlay_text")
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTextDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Watermark dialog
        if (showWatermarkDialog) {
            var watermarkText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showWatermarkDialog = false },
                title = { Text("Add Diagonal Watermark") },
                text = {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text (e.g. CONFIDENTIAL)") },
                        modifier = Modifier.fillMaxWidth().testTag("watermark_text_field")
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (watermarkText.isNotEmpty()) {
                                viewModel.addTextOverlayToCurrentPage(
                                    text = watermarkText.uppercase(),
                                    x = 0.5f,
                                    y = 0.5f,
                                    isHeader = false,
                                    isWatermark = true
                                )
                                showWatermarkDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_watermark_text")
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWatermarkDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ToolbarButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
