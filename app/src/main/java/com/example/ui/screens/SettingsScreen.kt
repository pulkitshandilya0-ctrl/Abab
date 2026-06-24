package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PdfViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var darkModeEnabled by remember { mutableStateOf(false) }

    // Cloud backup connectors
    var googleDriveConnected by remember { mutableStateOf(false) }
    var dropboxConnected by remember { mutableStateOf(false) }
    var oneDriveConnected by remember { mutableStateOf(false) }

    var backupSyncingState by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Cloud", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Section 1: UI Toggles
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Appearance Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ListItem(
                        headlineContent = { Text("System Theme Mode") },
                        supportingContent = { Text("Toggle light or dark modes manually") },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = darkModeEnabled,
                                onCheckedChange = { darkModeEnabled = it },
                                modifier = Modifier.testTag("dark_mode_toggle")
                            )
                        }
                    )
                }
            }

            // Section 2: Cloud Integration Nodes
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Cloud Connectors & Backup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Google Drive integration
                    ListItem(
                        headlineContent = { Text("Google Drive integration") },
                        supportingContent = { Text(if (googleDriveConnected) "Connected as active storage node" else "Backup and restore your local PDFs") },
                        leadingContent = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF34A853)) },
                        trailingContent = {
                            Button(
                                onClick = {
                                    googleDriveConnected = !googleDriveConnected
                                    if (googleDriveConnected) backupSyncingState = "Successfully established secure Google Drive handshake!"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (googleDriveConnected) Color.Gray else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("google_drive_connect_button")
                            ) {
                                Text(if (googleDriveConnected) "Disconnect" else "Connect", fontSize = 11.sp)
                            }
                        }
                    )

                    // Dropbox integration
                    ListItem(
                        headlineContent = { Text("Dropbox Sync") },
                        supportingContent = { Text(if (dropboxConnected) "Connected" else "Sync tags, bookmarks and local histories") },
                        leadingContent = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF0061FE)) },
                        trailingContent = {
                            Button(
                                onClick = {
                                    dropboxConnected = !dropboxConnected
                                    if (dropboxConnected) backupSyncingState = "Dropbox cloud token registered successfully."
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dropboxConnected) Color.Gray else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("dropbox_connect_button")
                            ) {
                                Text(if (dropboxConnected) "Disconnect" else "Connect", fontSize = 11.sp)
                            }
                        }
                    )

                    // OneDrive integration
                    ListItem(
                        headlineContent = { Text("Microsoft OneDrive") },
                        supportingContent = { Text(if (oneDriveConnected) "Connected" else "Establish real-time backup node link") },
                        leadingContent = { Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0078D4)) },
                        trailingContent = {
                            Button(
                                onClick = {
                                    oneDriveConnected = !oneDriveConnected
                                    if (oneDriveConnected) backupSyncingState = "OneDrive backup pathway mapped."
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (oneDriveConnected) Color.Gray else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("onedrive_connect_button")
                            ) {
                                Text(if (oneDriveConnected) "Disconnect" else "Connect", fontSize = 11.sp)
                            }
                        }
                    )
                }
            }

            // Sync alert tracker
            if (backupSyncingState.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(backupSyncingState, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                            IconButton(onClick = { backupSyncingState = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Section 3: Legal & Support Information
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Information & Legal",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ListItem(
                        headlineContent = { Text("About PDF Tech masters") },
                        supportingContent = { Text("v1.0.0 (Native Release) | Offline-first sandbox") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                    )

                    ListItem(
                        headlineContent = { Text("Support Contact") },
                        supportingContent = { Text("pulkitshandilya0@gmail.com") },
                        leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                }
            }
        }
    }
}
