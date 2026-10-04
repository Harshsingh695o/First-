package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WallpaperConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MockApp(
    val name: String,
    val icon: ImageVector,
    val colorStart: Color,
    val colorEnd: Color
)

@Composable
fun HomeScreenMockupOverlay(
    config: WallpaperConfig,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val pageOffset by animateFloatAsState(
        targetValue = currentPage * 40f,
        label = "pageOffset"
    )

    val allApps = listOf(
        MockApp("Camera", Icons.Default.CameraAlt, Color(0xFF38BDF8), Color(0xFF0284C7)),
        MockApp("Gallery", Icons.Default.Photo, Color(0xFFF43F5E), Color(0xFFBE123C)),
        MockApp("Music", Icons.Default.MusicNote, Color(0xFFA855F7), Color(0xFF7E22CE)),
        MockApp("Browser", Icons.Default.Language, Color(0xFF10B981), Color(0xFF047857)),
        MockApp("Settings", Icons.Default.Settings, Color(0xFF64748B), Color(0xFF334155)),
        MockApp("Files", Icons.Default.Folder, Color(0xFFF59E0B), Color(0xFFB45309)),
        MockApp("Explore", Icons.Default.Explore, Color(0xFF06B6D4), Color(0xFF0E7490)),
        MockApp("Video", Icons.Default.Videocam, Color(0xFFEC4899), Color(0xFFBE185D)),
        MockApp("Weather", Icons.Default.WbSunny, Color(0xFFFBBF24), Color(0xFFD97706)),
        MockApp("Messages", Icons.Default.Chat, Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        MockApp("Phone", Icons.Default.Phone, Color(0xFF22C55E), Color(0xFF15803D)),
        MockApp("Assistant", Icons.Default.Mic, Color(0xFF8B5CF6), Color(0xFF6D28D9))
    )

    val visibleAppsCount = (config.gridRows * config.gridCols).coerceAtMost(allApps.size)
    val displayApps = allApps.take(visibleAppsCount)

    val dateFormatted = remember {
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    },
                    onDragEnd = {
                        if (dragAccumulator > 60f && currentPage > 0) {
                            currentPage--
                        } else if (dragAccumulator < -60f && currentPage < 2) {
                            currentPage++
                        }
                        dragAccumulator = 0f
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // At A Glance / Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFormatted,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "24°C • Mostly Clear",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Icon Grid Simulator
            if (config.showAppIcons) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(config.gridCols),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = (-pageOffset).dp)
                            .testTag("home_app_grid"),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayApps.size) { index ->
                            val app = displayApps[index]
                            AppIconItem(app = app, iconStyle = config.iconStyle)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .testTag("home_page_dots"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (p in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(if (p == currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (p == currentPage) Color.White else Color.White.copy(alpha = 0.35f)
                            )
                            .clickable { currentPage = p }
                    )
                }
            }

            // Interactive Search Bar Widget
            if (config.showSearchBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0x700F172A))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search apps, web & motion...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smart Dock at bottom
            if (config.showDock) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x55000000))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("home_dock")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dockApps = listOf(
                            MockApp("Phone", Icons.Default.Phone, Color(0xFF22C55E), Color(0xFF15803D)),
                            MockApp("Messages", Icons.Default.Chat, Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                            MockApp("Browser", Icons.Default.Language, Color(0xFF10B981), Color(0xFF047857)),
                            MockApp("Camera", Icons.Default.CameraAlt, Color(0xFFF43F5E), Color(0xFFBE123C))
                        )
                        dockApps.forEach { app ->
                            AppIconItem(app = app, iconStyle = config.iconStyle, isDock = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconItem(
    app: MockApp,
    iconStyle: String,
    isDock: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = {})
            .padding(4.dp)
    ) {
        val iconShape = RoundedCornerShape(16.dp)

        when (iconStyle) {
            "MINIMAL" -> {
                Box(
                    modifier = Modifier
                        .size(if (isDock) 48.dp else 44.dp)
                        .clip(iconShape)
                        .background(Color(0x801E293B))
                        .border(1.dp, Color(0x40FFFFFF), iconShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = app.icon,
                        contentDescription = app.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            "GLASS" -> {
                Box(
                    modifier = Modifier
                        .size(if (isDock) 48.dp else 44.dp)
                        .clip(iconShape)
                        .background(Color(0x35FFFFFF))
                        .border(1.5.dp, Color(0x60FFFFFF), iconShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = app.icon,
                        contentDescription = app.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            else -> { // "NEON"
                Box(
                    modifier = Modifier
                        .size(if (isDock) 48.dp else 44.dp)
                        .clip(iconShape)
                        .background(
                            Brush.linearGradient(listOf(app.colorStart, app.colorEnd))
                        )
                        .border(1.dp, Color(0x40FFFFFF), iconShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = app.icon,
                        contentDescription = app.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (!isDock) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
