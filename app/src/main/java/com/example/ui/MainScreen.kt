package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DoubleTapAction
import com.example.data.ScreenType
import com.example.data.WallpaperConfig
import com.example.ui.components.ApplyWallpaperDialog
import com.example.ui.components.HomeScreenMockupOverlay
import com.example.ui.components.InteractiveVideoCanvas
import com.example.ui.components.LayoutCustomizerSheet
import com.example.ui.components.LockScreenMockupOverlay
import com.example.ui.components.VideoCustomizerSheet
import com.example.ui.components.VideoLibraryScreen
import androidx.compose.material.icons.filled.VideoLibrary

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val config by viewModel.configState.collectAsState()
    val libraryVideos by viewModel.libraryVideos.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val showApplyDialog by viewModel.showApplyDialog.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090D16),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131A29),
                contentColor = Color(0xFF38BDF8),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(width = 1.dp, color = Color(0x22FFFFFF))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Studio") },
                    label = { Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_studio")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_library")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Presets") },
                    label = { Text("Motion", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = Color(0xFFA855F7),
                        indicatorColor = Color(0xFFA855F7),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_video")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.DashboardCustomize, contentDescription = "Layout") },
                    label = { Text("Layout", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = Color(0xFFF472B6),
                        indicatorColor = Color(0xFFF472B6),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_layout")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Top App Bar
            TopBarSection(
                screenMode = config.activePreviewScreen,
                onToggleScreenMode = { viewModel.toggleScreenMode() },
                onApplyClick = { viewModel.openApplyDialog() }
            )

            // Content Body
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> {
                        // Fullscreen Interactive Studio
                        InteractiveStudioView(
                            config = config,
                            onToggleScreenMode = { viewModel.toggleScreenMode() },
                            onToggleMute = {
                                viewModel.updateConfig { it.copy(isMuted = !config.isMuted) }
                            },
                            onDoubleTap = { action -> viewModel.handleDoubleTapAction(action) }
                        )
                    }

                    1 -> {
                        // Stored Video Library
                        VideoLibraryScreen(
                            videos = libraryVideos,
                            activeConfig = config,
                            isImporting = isImporting,
                            onUploadVideo = { uri -> viewModel.uploadVideo(uri) },
                            onSelectAsWallpaper = { video -> viewModel.selectVideoFromLibrary(video) },
                            onToggleFavorite = { id, fav -> viewModel.toggleFavorite(id, fav) },
                            onRenameVideo = { id, title -> viewModel.renameVideo(id, title) },
                            onDeleteVideo = { video -> viewModel.deleteVideo(video) },
                            onPreseedPresets = { viewModel.preseedPresets() }
                        )
                    }

                    2 -> {
                        // Video Presets & Motion customizer
                        VideoCustomizerSheet(
                            config = config,
                            onConfigChange = { update -> viewModel.updateConfig(update) }
                        )
                    }

                    3 -> {
                        // Layout & Interactive Elements customizer
                        LayoutCustomizerSheet(
                            config = config,
                            onConfigChange = { update -> viewModel.updateConfig(update) }
                        )
                    }
                }
            }
        }

        // Apply Wallpaper Dialog
        if (showApplyDialog) {
            ApplyWallpaperDialog(
                onDismiss = { viewModel.closeApplyDialog() }
            )
        }
    }
}

@Composable
private fun TopBarSection(
    screenMode: ScreenType,
    onToggleScreenMode: () -> Unit,
    onApplyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Glowing Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "LuminaMotion",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Dynamic Video Wallpaper",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Buttons: Quick Switch Pill & Apply Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Mode toggle pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x331E293B))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .clickable { onToggleScreenMode() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_screen_mode_pill"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (screenMode == ScreenType.LOCK_SCREEN) Icons.Default.Lock else Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (screenMode == ScreenType.LOCK_SCREEN) "Lock" else "Home",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Primary Apply CTA Button
            Button(
                onClick = onApplyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8)
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("top_bar_apply_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apply",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InteractiveStudioView(
    config: WallpaperConfig,
    onToggleScreenMode: () -> Unit,
    onToggleMute: () -> Unit,
    onDoubleTap: (com.example.data.DoubleTapAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Phone Device Mockup Container
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .border(2.5.dp, Color(0x4038BDF8), RoundedCornerShape(32.dp))
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Layer 1: Hardware-Accelerated Dynamic Video Canvas
                InteractiveVideoCanvas(
                    config = config,
                    onDoubleTapTriggered = onDoubleTap,
                    modifier = Modifier.fillMaxSize()
                )

                // Layer 2: Interactive Screen Mockup Overlay (Lock vs Home)
                if (config.activePreviewScreen == ScreenType.LOCK_SCREEN) {
                    LockScreenMockupOverlay(
                        config = config,
                        onClockClicked = onToggleScreenMode,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    HomeScreenMockupOverlay(
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Layer 3: Floating Interactive Helper Pill (Tips on gesture & sound)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x70000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touch screen to test ripples • Double-tap to ${config.doubleTapAction.label}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Layer 4: Quick Audio Floating Button (Top right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 54.dp, end = 12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x800F172A))
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                        .clickable { onToggleMute() }
                        .testTag("quick_mute_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (config.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute Toggle",
                        tint = if (config.isMuted) Color(0xFF94A3B8) else Color(0xFF34D399),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
