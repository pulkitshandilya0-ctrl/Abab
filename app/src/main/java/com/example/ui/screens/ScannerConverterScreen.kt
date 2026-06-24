package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfFile
import com.example.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerConverterScreen(
    viewModel: PdfViewModel,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPdfs by viewModel.allPdfs.collectAsState()
    val conversionStatus by viewModel.conversionStatus.collectAsState()

    val ocrResultText by viewModel.ocrResultText.collectAsState()
    val isOcrProcessing by viewModel.isOcrProcessing.collectAsState()

    var activeScannerTab by remember { mutableStateOf(0) } // 0 = Scanner, 1 = Converter

    // Conversion Config states
    var convertFromFormat by remember { mutableStateOf("Word (.docx)") }
    var convertToFormat by remember { mutableStateOf("PDF (.pdf)") }
    var selectedPdfToConvert by remember { mutableStateOf<PdfFile?>(null) }

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    var showPdfDropdown by remember { mutableStateOf(false) }

    val formatsList = listOf("Word (.docx)", "Excel (.xlsx)", "PowerPoint (.pptx)", "Image (.jpg)", "Image (.png)", "Plain Text (.txt)", "HTML (.html)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools Hub", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scanner vs Converter Tabs Header
            TabRow(
                selectedTabIndex = activeScannerTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeScannerTab == 0,
                    onClick = { activeScannerTab = 0 },
                    text = { Text("Camera Document Scanner", fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_camera_scanner")
                )
                Tab(
                    selected = activeScannerTab == 1,
                    onClick = { activeScannerTab = 1 },
                    text = { Text("File Converter Engine", fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_file_converter")
                )
            }

            Crossfade(targetState = activeScannerTab, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> CameraScannerView(
                        ocrResultText = ocrResultText,
                        isOcrProcessing = isOcrProcessing,
                        onTriggerOcrScan = {
                            // Generate mock bitmap and send to OCR processor
                            val mockBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            viewModel.scanDocumentAndProcessOcr(mockBitmap)
                        },
                        onSearchQuery = { query ->
                            viewModel.searchOcrText(query)
                        }
                    )
                    1 -> ConversionEngineView(
                        allPdfs = allPdfs,
                        conversionStatus = conversionStatus,
                        convertFromFormat = convertFromFormat,
                        convertToFormat = convertToFormat,
                        selectedPdf = selectedPdfToConvert,
                        showFromDropdown = showFromDropdown,
                        showToDropdown = showToDropdown,
                        showPdfDropdown = showPdfDropdown,
                        formatsList = formatsList,
                        onFromToggle = { showFromDropdown = !showFromDropdown },
                        onToToggle = { showToDropdown = !showToDropdown },
                        onPdfToggle = { showPdfDropdown = !showPdfDropdown },
                        onFromSelect = {
                            convertFromFormat = it
                            if (it != "PDF (.pdf)") {
                                convertToFormat = "PDF (.pdf)"
                            } else {
                                convertToFormat = "Word (.docx)"
                            }
                            showFromDropdown = false
                        },
                        onToSelect = {
                            convertToFormat = it
                            showToDropdown = false
                        },
                        onPdfSelect = {
                            selectedPdfToConvert = it
                            showPdfDropdown = false
                        },
                        onTriggerConvert = {
                            if (convertFromFormat == "PDF (.pdf)") {
                                viewModel.convertFile("PDF", convertToFormat.substringBefore(" (").trim(), selectedPdfToConvert)
                            } else {
                                viewModel.convertFile(convertFromFormat.substringBefore(" (").trim(), "PDF", null)
                            }
                        },
                        onClearStatus = { viewModel.clearConversionStatus() }
                    )
                }
            }
        }
    }
}

// --- SUB-VIEWS FOR CAMERA SCANNER & CONVERSION PANELS ---

@Composable
fun CameraScannerView(
    ocrResultText: String,
    isOcrProcessing: Boolean,
    onTriggerOcrScan: () -> Unit,
    onSearchQuery: (String) -> List<String>
) {
    var searchQueryText by remember { mutableStateOf("") }
    val searchResults = remember(searchQueryText, ocrResultText) {
        if (searchQueryText.trim().isEmpty()) emptyList()
        else onSearchQuery(searchQueryText)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera Viewfinder Simulation Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(0.4f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background simulated document frame
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterCenterFocus,
                        contentDescription = null,
                        tint = Color.Green.copy(0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "AUTO EDGE DETECTION ON",
                        color = Color.Green,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Align your paper document inside corners",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Decorative camera scanning borders
                Box(modifier = Modifier.align(Alignment.TopStart).padding(20.dp).size(24.dp).border(2.dp, Color.White, RoundedCornerShape(topStart = 8.dp)))
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(24.dp).border(2.dp, Color.White, RoundedCornerShape(topEnd = 8.dp)))
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp).size(24.dp).border(2.dp, Color.White, RoundedCornerShape(bottomStart = 8.dp)))
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).size(24.dp).border(2.dp, Color.White, RoundedCornerShape(bottomEnd = 8.dp)))

                // Quick scan trigger floating button
                FloatingActionButton(
                    onClick = onTriggerOcrScan,
                    shape = CircleShape,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .testTag("scan_shutter_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture Document")
                }
            }
        }

        // Live OCR status/progress
        if (isOcrProcessing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            ocrResultText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // OCR Result display
        if (ocrResultText.isNotEmpty() && !isOcrProcessing) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "ML Kit Extracted Text",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Text Search in OCR Result
                    OutlinedTextField(
                        value = searchQueryText,
                        onValueChange = { searchQueryText = it },
                        placeholder = { Text("Search text in scanned result...") },
                        leadingIcon = { Icon(Icons.Default.FindInPage, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .testTag("ocr_text_search")
                    )

                    // Highlight matches
                    if (searchResults.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Matches Found:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                searchResults.forEach { match ->
                                    Text("• $match", fontSize = 12.sp, color = Color.Black, modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                ocrResultText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversionEngineView(
    allPdfs: List<PdfFile>,
    conversionStatus: String,
    convertFromFormat: String,
    convertToFormat: String,
    selectedPdf: PdfFile?,
    showFromDropdown: Boolean,
    showToDropdown: Boolean,
    showPdfDropdown: Boolean,
    formatsList: List<String>,
    onFromToggle: () -> Unit,
    onToToggle: () -> Unit,
    onPdfToggle: () -> Unit,
    onFromSelect: (String) -> Unit,
    onToSelect: (String) -> Unit,
    onPdfSelect: (PdfFile) -> Unit,
    onTriggerConvert: () -> Unit,
    onClearStatus: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Selection cards for formats
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Conversion Setup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Convert From
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text("Convert From", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onFromToggle() }
                                        .padding(10.dp)
                                        .testTag("from_format_selector")
                                ) {
                                    Text(convertFromFormat, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(expanded = showFromDropdown, onDismissRequest = onFromToggle) {
                                (formatsList + "PDF (.pdf)").forEach { format ->
                                    DropdownMenuItem(text = { Text(format) }, onClick = { onFromSelect(format) })
                                }
                            }
                        }

                        // Arrow
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 16.dp)
                        )

                        // Convert To
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text("Convert To", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onToToggle() }
                                        .padding(10.dp)
                                        .testTag("to_format_selector")
                                ) {
                                    Text(convertToFormat, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(expanded = showToDropdown, onDismissRequest = onToToggle) {
                                if (convertFromFormat == "PDF (.pdf)") {
                                    formatsList.forEach { format ->
                                        DropdownMenuItem(text = { Text(format) }, onClick = { onToSelect(format) })
                                    }
                                } else {
                                    DropdownMenuItem(text = { Text("PDF (.pdf)") }, onClick = { onToSelect("PDF (.pdf)") })
                                }
                            }
                        }
                    }

                    // Selected file selector (if converting FROM PDF)
                    if (convertFromFormat == "PDF (.pdf)") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select PDF Source File", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onPdfToggle() }
                                    .padding(12.dp)
                                    .testTag("source_pdf_selector")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                Text(selectedPdf?.name ?: "Choose PDF from Workspace...", fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(expanded = showPdfDropdown, onDismissRequest = onPdfToggle) {
                                allPdfs.forEach { pdf ->
                                    DropdownMenuItem(text = { Text(pdf.name) }, onClick = { onPdfSelect(pdf) })
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Conversion Progress Tracker
        if (conversionStatus.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (conversionStatus.contains("Successfully") || conversionStatus.contains("Error")) {
                                Icon(
                                    imageVector = if (conversionStatus.contains("Error")) Icons.Default.Error else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (conversionStatus.contains("Error")) Color.Red else Color.Green,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                conversionStatus,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (conversionStatus.contains("Successfully") || conversionStatus.contains("Error")) {
                                IconButton(onClick = onClearStatus) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trigger action
        item {
            Button(
                onClick = onTriggerConvert,
                enabled = (convertFromFormat != "PDF (.pdf)" || selectedPdf != null) && !conversionStatus.contains("Converting") && !conversionStatus.contains("Merging"),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("trigger_conversion_button")
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Process Conversion Task")
            }
        }
    }
}
