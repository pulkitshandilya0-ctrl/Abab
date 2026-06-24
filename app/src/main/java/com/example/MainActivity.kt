package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.PdfEditorScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ScannerConverterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PdfViewModel
import com.example.viewmodel.PdfViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Get repositories from PdfApplication
                val app = application as PdfApplication
                val viewModel: PdfViewModel by viewModels {
                    PdfViewModelFactory(app.pdfRepository, app.aiRepository)
                }

                AppNavigationContainer(viewModel = viewModel)
            }
        }
    }
}

sealed class NavigationItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    object FileManager : NavigationItem("file_manager", "Workspace", Icons.Default.FolderSpecial)
    object ScannerConverter : NavigationItem("scanner", "Tools Hub", Icons.Default.Category)
    object AiAssistant : NavigationItem("ai_assistant", "Gemini AI", Icons.Default.AutoAwesome)
}

@Composable
fun AppNavigationContainer(viewModel: PdfViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom Navigation Bar is visible only on core dashboard pages to keep clean screen footprint
    val bottomNavRoutes = listOf("dashboard", "file_manager", "scanner", "ai_assistant")
    val shouldShowBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    val navItems = listOf(
                        NavigationItem.Dashboard,
                        NavigationItem.FileManager,
                        NavigationItem.ScannerConverter,
                        NavigationItem.AiAssistant
                    )

                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (shouldShowBottomBar) innerPadding.calculateBottomPadding() else innerPadding.calculateTopPadding() * 0)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            
            composable("file_manager") {
                FileManagerScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            composable("pdf_editor") {
                PdfEditorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("ai_assistant") {
                AiAssistantScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            composable("scanner") {
                ScannerConverterScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }

            composable("premium") {
                PremiumScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
