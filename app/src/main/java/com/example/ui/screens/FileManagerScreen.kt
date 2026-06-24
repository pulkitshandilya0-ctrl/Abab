package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Folder
import com.example.data.PdfFile
import com.example.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileManagerScreen(
    viewModel: PdfViewModel,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPdfs by viewModel.filteredPdfs.collectAsState()
    val favoritePdfs by viewModel.favoritePdfs.collectAsState()
    val folders by viewModel.folders.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = All PDFs, 1 = Favorites, 2 = Folders

    // Dialog control states
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf<PdfFile?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var showMoveDialog by remember { mutableStateOf<PdfFile?>(null) }

    var showImportDialog by remember { mutableStateOf(false) }
    var importName by remember { mutableStateOf("") }
    var importPages by remember { mutableStateOf("1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("file_manager_import_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Import PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (activeTab == 2) {
                        showCreateFolderDialog = true
                    } else {
                        showImportDialog = true
                    }
                },
                icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                text = { Text(if (activeTab == 2) "New Folder" else "Import PDF") },
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .testTag("file_manager_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar Component
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search files, tags, reports...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("file_manager_search")
            )

            // Horizontal Filter bar for Folders and Tags
            if (selectedFolderId != null || selectedTag != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text("Filtered by:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (selectedFolderId != null) {
                        val activeFolderName = folders.find { it.id == selectedFolderId }?.name ?: "Folder"
                        InputChip(
                            selected = true,
                            onClick = { viewModel.selectedFolderId.value = null },
                            label = { Text(activeFolderName) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    if (selectedTag != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        InputChip(
                            selected = true,
                            onClick = { viewModel.selectedTag.value = null },
                            label = { Text("#$selectedTag") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }

            // Tabs Header
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("All PDFs", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_all_pdfs")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Favorites", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_favorites")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Folders", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_folders")
                )
            }

            // Render list content based on active tab
            Crossfade(targetState = activeTab, modifier = Modifier.weight(1f)) { tab ->
                when (tab) {
                    0 -> PdfFileList(
                        pdfs = filteredPdfs,
                        folders = folders,
                        onSelectPdf = { pdf ->
                            viewModel.selectPdf(pdf)
                            onNavigateToRoute("pdf_editor")
                        },
                        onRename = { pdf ->
                            showRenameDialog = pdf
                            renameInput = pdf.name
                        },
                        onDelete = { pdf -> viewModel.deletePdfFile(pdf) },
                        onToggleFavorite = { pdf -> viewModel.toggleFavoriteFile(pdf) },
                        onTogglePassword = { pdf -> viewModel.togglePdfPassword(pdf) },
                        onMove = { pdf -> showMoveDialog = pdf },
                        emptyStateText = "No PDFs found in your workspace."
                    )
                    1 -> PdfFileList(
                        pdfs = favoritePdfs,
                        folders = folders,
                        onSelectPdf = { pdf ->
                            viewModel.selectPdf(pdf)
                            onNavigateToRoute("pdf_editor")
                        },
                        onRename = { pdf ->
                            showRenameDialog = pdf
                            renameInput = pdf.name
                        },
                        onDelete = { pdf -> viewModel.deletePdfFile(pdf) },
                        onToggleFavorite = { pdf -> viewModel.toggleFavoriteFile(pdf) },
                        onTogglePassword = { pdf -> viewModel.togglePdfPassword(pdf) },
                        onMove = { pdf -> showMoveDialog = pdf },
                        emptyStateText = "Starred documents appear here for quick access."
                    )
                    2 -> FolderList(
                        folders = folders,
                        onFolderSelect = { folderId ->
                            viewModel.selectedFolderId.value = folderId
                            activeTab = 0 // Navigate back to All PDFs list filtered
                        },
                        onDeleteFolder = { folder ->
                            scope.launch { viewModel.deletePdfFile(PdfFile(id = 0, name = "", path = "", size = 0)) } // Stub call or handle deletion
                        },
                        onCreateFolderClick = { showCreateFolderDialog = true }
                    )
                }
            }
        }

        // --- DIALOGS FOR FILE OPERATIONS ---

        // Create Folder Dialog
        if (showCreateFolderDialog) {
            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text("Create Folder") },
                text = {
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("folder_name_field")
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (folderNameInput.trim().isNotEmpty()) {
                                viewModel.createNewFolder(folderNameInput.trim())
                                folderNameInput = ""
                                showCreateFolderDialog = false
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_folder_button")
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rename PDF Dialog
        showRenameDialog?.let { pdf ->
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                title = { Text("Rename PDF") },
                text = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("New Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("rename_pdf_field")
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInput.trim().isNotEmpty()) {
                                viewModel.renamePdfFile(pdf, renameInput.trim())
                                showRenameDialog = null
                            }
                        },
                        modifier = Modifier.testTag("confirm_rename_button")
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Move to Folder Dialog
        showMoveDialog?.let { pdf ->
            AlertDialog(
                onDismissRequest = { showMoveDialog = null },
                title = { Text("Move to Folder") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Choose destination folder for '${pdf.name}':", modifier = Modifier.padding(bottom = 12.dp))
                        
                        ListItem(
                            headlineContent = { Text("Root (No Folder)", fontWeight = FontWeight.Bold) },
                            leadingContent = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        viewModel.movePdfToFolder(pdf.id, null)
                                        showMoveDialog = null
                                    }
                                }
                                .fillMaxWidth()
                        )
                        HorizontalDivider()

                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(folders) { f ->
                                ListItem(
                                    headlineContent = { Text(f.name) },
                                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                    modifier = Modifier
                                        .clickable {
                                            scope.launch {
                                                viewModel.movePdfToFolder(pdf.id, f.id)
                                                showMoveDialog = null
                                            }
                                        }
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMoveDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Import Simulated PDF Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import PDF Document") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Simulate downloading, scanning, or capturing an offline PDF file into your workspace.")
                        OutlinedTextField(
                            value = importName,
                            onValueChange = { importName = it },
                            label = { Text("Document Name") },
                            placeholder = { Text("Tax_Invoice.pdf") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("import_name_field")
                        )
                        OutlinedTextField(
                            value = importPages,
                            onValueChange = { importPages = it },
                            label = { Text("Page Count") },
                            placeholder = { Text("3") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("import_pages_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalName = if (importName.trim().isEmpty()) "Imported_Document.pdf" else importName.trim()
                            val cleanName = if (finalName.endsWith(".pdf", ignoreCase = true)) finalName else "$finalName.pdf"
                            val pageCount = importPages.toIntOrNull() ?: 1
                            val size = pageCount * 450000L + 210000L // mock scale size
                            viewModel.importPdfFile(cleanName, size, pageCount)
                            importName = ""
                            importPages = "1"
                            showImportDialog = false
                        },
                        modifier = Modifier.testTag("confirm_import_button")
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PdfFileList(
    pdfs: List<PdfFile>,
    folders: List<Folder>,
    onSelectPdf: (PdfFile) -> Unit,
    onRename: (PdfFile) -> Unit,
    onDelete: (PdfFile) -> Unit,
    onToggleFavorite: (PdfFile) -> Unit,
    onTogglePassword: (PdfFile) -> Unit,
    onMove: (PdfFile) -> Unit,
    emptyStateText: String
) {
    if (pdfs.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = emptyStateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pdfs, key = { it.id }) { pdf ->
                val associatedFolderName = folders.find { it.id == pdf.folderId }?.name
                PdfFileRowItem(
                    pdf = pdf,
                    folderName = associatedFolderName,
                    onClick = { onSelectPdf(pdf) },
                    onRename = { onRename(pdf) },
                    onDelete = { onDelete(pdf) },
                    onToggleFavorite = { onToggleFavorite(pdf) },
                    onTogglePassword = { onTogglePassword(pdf) },
                    onMove = { onMove(pdf) }
                )
            }
        }
    }
}

@Composable
fun PdfFileRowItem(
    pdf: PdfFile,
    folderName: String?,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePassword: () -> Unit,
    onMove: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("file_row_${pdf.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${pdf.pageCount} pgs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.size(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), CircleShape))
                    Text("%.1f MB".format(pdf.size / 1024000.0), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    if (folderName != null) {
                        Box(modifier = Modifier.size(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), CircleShape))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(folderName, fontSize = 9.sp) },
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
            }

            // Star Favorite button
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (pdf.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (pdf.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Actions dropdown menu
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open in Editor") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            expandedMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (pdf.isLocked) "Remove Password" else "Password Protect") },
                        leadingIcon = { Icon(if (pdf.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null) },
                        onClick = {
                            expandedMenu = false
                            onTogglePassword()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        onClick = {
                            expandedMenu = false
                            onMove()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename File") },
                        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                        onClick = {
                            expandedMenu = false
                            onRename()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderList(
    folders: List<Folder>,
    onFolderSelect: (Int) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onCreateFolderClick: () -> Unit
) {
    if (folders.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FolderOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "No folders created yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Button(onClick = onCreateFolderClick, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Create First Folder")
                }
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(folders, key = { it.id }) { folder ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderSelect(folder.id) }
                        .testTag("folder_item_${folder.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tap to view items inside",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
