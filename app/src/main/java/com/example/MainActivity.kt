package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sanjutools.ads.AdManager
import com.example.sanjutools.model.ToolCategory
import com.example.sanjutools.ui.components.AppTopBar
import com.example.sanjutools.ui.screens.DashboardScreen
import com.example.sanjutools.ui.screens.DiscoverToolsScreen
import com.example.sanjutools.ui.screens.SettingsScreen
import com.example.sanjutools.ui.screens.ToolHostScreen
import com.example.sanjutools.viewmodel.MainViewModel
import com.example.sanjutools.viewmodel.ThemeMode
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK with caching
        AdManager.getInstance().initialize(this)

        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val activeToolId by viewModel.activeToolId.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()

            var isSettingsOpen by remember { mutableStateOf(false) }
            var isDiscoverOpen by remember { mutableStateOf(false) }

            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        if (activeToolId == null && !isSettingsOpen && !isDiscoverOpen) {
                            AppTopBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                isDarkTheme = isDark,
                                onToggleTheme = {
                                    viewModel.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK)
                                },
                                onOpenSettings = { isSettingsOpen = true },
                                onOpenDiscover = { isDiscoverOpen = true }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when {
                            isSettingsOpen -> {
                                BackHandler { isSettingsOpen = false }
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { isSettingsOpen = false }
                                )
                            }
                            isDiscoverOpen -> {
                                BackHandler { isDiscoverOpen = false }
                                DiscoverToolsScreen(
                                    tool = null,
                                    viewModel = viewModel,
                                    onOpenTool = { toolId ->
                                        isDiscoverOpen = false
                                        AdManager.getInstance().showInterstitialIfAllowed(this@MainActivity) {
                                            viewModel.openTool(toolId)
                                        }
                                    },
                                    onBack = { isDiscoverOpen = false }
                                )
                            }
                            activeToolId != null -> {
                                ToolHostScreen(
                                    toolId = activeToolId!!,
                                    viewModel = viewModel,
                                    onBack = {
                                        AdManager.getInstance().showInterstitialIfAllowed(this@MainActivity) {
                                            viewModel.closeTool()
                                        }
                                    }
                                )
                            }
                            else -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onOpenTool = { toolId ->
                                        AdManager.getInstance().showInterstitialIfAllowed(this@MainActivity) {
                                            viewModel.openTool(toolId)
                                        }
                                    },
                                    onOpenDiscover = { isDiscoverOpen = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
