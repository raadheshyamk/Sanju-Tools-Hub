package com.example.sanjutools.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolCategory
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.model.ToolRegistry
import com.example.sanjutools.ui.components.CategoryChipBar
import com.example.sanjutools.ui.components.ToolCard
import com.example.sanjutools.viewmodel.MainViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenTool: (String) -> Unit,
    onOpenDiscover: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tools by viewModel.filteredTools.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val recentHistory by viewModel.recentHistory.collectAsState()

    // Quick filter tab: All, Favorites, Popular, Recent
    var quickFilter by remember { mutableStateOf("ALL") } // ALL, RECENT, FAVORITES, POPULAR

    val displayedTools = remember(tools, quickFilter, favoriteIds, recentHistory) {
        when (quickFilter) {
            "FAVORITES" -> ToolRegistry.tools.filter { favoriteIds.contains(it.id) }
            "POPULAR" -> ToolRegistry.tools.filter { it.isPopular }
            "RECENT" -> {
                val recentIds = recentHistory.map { it.toolId }.distinct()
                ToolRegistry.tools.filter { recentIds.contains(it.id) }
            }
            else -> tools
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columnsCount = when {
            maxWidth >= 1000.dp -> 6
            maxWidth >= 720.dp -> 4
            maxWidth >= 380.dp -> 3
            else -> 2 // Two columns on small phones
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hero Welcome & Discovery Banner
            item(span = { GridItemSpan(columnsCount) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandBlueDark,
                                        BrandPurpleDark
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "⚡ Instant Offline Power",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.OfflinePin,
                                        contentDescription = null,
                                        tint = BrandCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "100% Private",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Every utility tool you need in one place.",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "121 offline working tools for photos, QR codes, PDF editing, rich writing, scientific math, and daily productivity.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.sanju_logo),
                                    contentDescription = "Sanju Tools Hub Official Logo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, BrandCyan, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Filter Tabs (All, Favorites, Popular, Recently Used)
            item(span = { GridItemSpan(columnsCount) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair("ALL", "All (${ToolRegistry.tools.size})"),
                        Pair("POPULAR", "Popular 🔥"),
                        Pair("FAVORITES", "Favorites ⭐ (${favoriteIds.size})"),
                        Pair("RECENT", "Recent 🕒")
                    ).forEach { (key, label) ->
                        val isSelected = quickFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { quickFilter = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Categories horizontal bar (when quickFilter == ALL)
            if (quickFilter == "ALL") {
                item(span = { GridItemSpan(columnsCount) }) {
                    CategoryChipBar(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }
            }

            // Tool Section Header
            item(span = { GridItemSpan(columnsCount) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (quickFilter) {
                            "FAVORITES" -> "Your Starred Tools"
                            "POPULAR" -> "Popular & Trending Tools"
                            "RECENT" -> "Recently Used Tools"
                            else -> selectedCategory.title
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${displayedTools.size} tools",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tools Cards Grid
            if (displayedTools.isEmpty()) {
                item(span = { GridItemSpan(columnsCount) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No tools found matching your search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                viewModel.updateSearchQuery("")
                                viewModel.selectCategory(ToolCategory.ALL)
                                quickFilter = "ALL"
                            }) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                items(displayedTools, key = { it.id }) { tool ->
                    ToolCard(
                        tool = tool,
                        isFavorite = favoriteIds.contains(tool.id),
                        onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                        onClick = { onOpenTool(tool.id) }
                    )
                }
            }

            // Discover Tools Promo Footer
            item(span = { GridItemSpan(columnsCount) }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDiscover() }
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Missing a tool? Discover more!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Smart AI finder & tool request roadmap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandPurple)
                    }
                }
            }
        }
    }
}
