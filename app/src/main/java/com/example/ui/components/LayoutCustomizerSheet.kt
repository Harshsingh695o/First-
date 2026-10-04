package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockStyle
import com.example.data.DoubleTapAction
import com.example.data.ScreenType
import com.example.data.TouchEffectType
import com.example.data.WallpaperConfig
import kotlin.math.roundToInt

@Composable
fun LayoutCustomizerSheet(
    config: WallpaperConfig,
    onConfigChange: ((WallpaperConfig) -> WallpaperConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Mode Switch: Lock Screen vs Home Screen
        TabRow(
            selectedTabIndex = if (config.activePreviewScreen == ScreenType.LOCK_SCREEN) 0 else 1,
            containerColor = Color(0x331E293B),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(
                        tabPositions[if (config.activePreviewScreen == ScreenType.LOCK_SCREEN) 0 else 1]
                    ),
                    color = Color(0xFF38BDF8),
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = config.activePreviewScreen == ScreenType.LOCK_SCREEN,
                onClick = { onConfigChange { it.copy(activePreviewScreen = ScreenType.LOCK_SCREEN) } },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock Screen Layout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_lock_screen")
            )
            Tab(
                selected = config.activePreviewScreen == ScreenType.HOME_SCREEN,
                onClick = { onConfigChange { it.copy(activePreviewScreen = ScreenType.HOME_SCREEN) } },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Home Screen Layout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_home_screen")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: Dynamic Clock & Typography (Primary focus for Lock & Home customization)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Dynamic Clock & Date Widget",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clock styles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockStyle.values().forEach { style ->
                FilterChip(
                    selected = config.clockStyle == style,
                    onClick = { onConfigChange { it.copy(clockStyle = style) } },
                    label = { Text(style.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("clock_style_${style.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clock Position (Y-Offset slider)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Vertical Position (Y-Offset)", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Text(text = "${(config.clockYOffset * 100).roundToInt()}%", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = config.clockYOffset,
            onValueChange = { y -> onConfigChange { it.copy(clockYOffset = y) } },
            valueRange = 0.06f..0.55f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF38BDF8)
            )
        )

        // Clock Color Palette Chips
        Text(text = "Accent Color", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val colors = listOf("#38BDF8", "#F43F5E", "#A855F7", "#10B981", "#F59E0B", "#FFFFFF")
            colors.forEach { hex ->
                val isColorSelected = config.clockColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(
                            width = if (isColorSelected) 3.dp else 1.dp,
                            color = if (isColorSelected) Color.White else Color(0x44000000),
                            shape = CircleShape
                        )
                        .clickable { onConfigChange { it.copy(clockColorHex = hex) } }
                        .testTag("color_chip_$hex"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isColorSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Toggles: Glow, Seconds, 24-Hour
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.clockGlow,
                onClick = { onConfigChange { it.copy(clockGlow = !config.clockGlow) } },
                label = { Text("Neon Glow", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA855F7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                )
            )
            FilterChip(
                selected = config.showSeconds,
                onClick = { onConfigChange { it.copy(showSeconds = !config.showSeconds) } },
                label = { Text("Seconds", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA855F7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                )
            )
            FilterChip(
                selected = config.use24Hour,
                onClick = { onConfigChange { it.copy(use24Hour = !config.use24Hour) } },
                label = { Text("24h Format", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA855F7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 2: Interactive Touch Effects
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = Color(0xFFF472B6),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Interactive Touch Effects",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Visual reaction when tapping anywhere on wallpaper",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TouchEffectType.values().forEach { fx ->
                FilterChip(
                    selected = config.touchEffect == fx,
                    onClick = { onConfigChange { it.copy(touchEffect = fx) } },
                    label = { Text(fx.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF472B6),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("touch_fx_${fx.name}")
                )
            }
        }

        // Haptic feedback & Double Tap Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x331E293B))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Touch Haptic Vibration", color = Color.White, fontSize = 13.sp)
            }
            Switch(
                checked = config.touchHaptics,
                onCheckedChange = { checked -> onConfigChange { it.copy(touchHaptics = checked) } },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF38BDF8),
                    checkedTrackColor = Color(0x5538BDF8)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Double-Tap Gesture Action", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DoubleTapAction.values().forEach { action ->
                FilterChip(
                    selected = config.doubleTapAction == action,
                    onClick = { onConfigChange { it.copy(doubleTapAction = action) } },
                    label = { Text(action.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 3: Screen-Specific Elements
        if (config.activePreviewScreen == ScreenType.LOCK_SCREEN) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lock Screen Interactive Widgets",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x331E293B))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                WidgetToggleRow(
                    title = "Live Battery Status Ring",
                    subtitle = "Shows real-time battery level & charging state",
                    checked = config.showBatteryWidget,
                    onCheckedChange = { c -> onConfigChange { it.copy(showBatteryWidget = c) } }
                )
                WidgetToggleRow(
                    title = "Live Weather Pill",
                    subtitle = "Interactive tap to cycle Celsius / Fahrenheit",
                    checked = config.showWeatherWidget,
                    onCheckedChange = { c -> onConfigChange { it.copy(showWeatherWidget = c) } }
                )
                WidgetToggleRow(
                    title = "Interactive Music Player Card",
                    subtitle = "Album art, animated equalizer, play/pause and skip buttons",
                    checked = config.showMusicWidget,
                    onCheckedChange = { c -> onConfigChange { it.copy(showMusicWidget = c) } }
                )
                WidgetToggleRow(
                    title = "Torch / Flashlight Trigger",
                    subtitle = "Quick bottom left trigger with beam feedback",
                    checked = config.showTorchTrigger,
                    onCheckedChange = { c -> onConfigChange { it.copy(showTorchTrigger = c) } }
                )
                WidgetToggleRow(
                    title = "Camera Quick Trigger",
                    subtitle = "Quick bottom right trigger with shutter flash",
                    checked = config.showCameraTrigger,
                    onCheckedChange = { c -> onConfigChange { it.copy(showCameraTrigger = c) } }
                )
            }
        } else {
            // Home Screen Specific Customizer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Home Screen Grid & Elements",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid Size selection (4x4, 4x5, 5x5)
            Text(text = "App Icon Grid Density", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val gridOptions = listOf(Pair(4, 4), Pair(5, 4), Pair(5, 5))
                gridOptions.forEach { (rows, cols) ->
                    val isSelected = config.gridRows == rows && config.gridCols == cols
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange { it.copy(gridRows = rows, gridCols = cols) } },
                        label = { Text("${cols} × $rows", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0x331E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            // Icon Aesthetic Styles (Neon, Glass, Minimal)
            Text(text = "Icon Aesthetics", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val styles = listOf("NEON" to "Neon Glow", "GLASS" to "Frosted Glass", "MINIMAL" to "Minimal Dark")
                styles.forEach { (key, label) ->
                    FilterChip(
                        selected = config.iconStyle == key,
                        onClick = { onConfigChange { it.copy(iconStyle = key) } },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0x331E293B),
                            labelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x331E293B))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                WidgetToggleRow(
                    title = "App Icons Overlay",
                    subtitle = "Simulate icons on top of dynamic video wallpaper",
                    checked = config.showAppIcons,
                    onCheckedChange = { c -> onConfigChange { it.copy(showAppIcons = c) } }
                )
                WidgetToggleRow(
                    title = "Search Bar Widget",
                    subtitle = "Lumina dynamic pill search widget",
                    checked = config.showSearchBar,
                    onCheckedChange = { c -> onConfigChange { it.copy(showSearchBar = c) } }
                )
                WidgetToggleRow(
                    title = "Bottom Smart Dock",
                    subtitle = "Quick access bar with phone, messages, browser",
                    checked = config.showDock,
                    onCheckedChange = { c -> onConfigChange { it.copy(showDock = c) } }
                )
            }
        }
    }
}

@Composable
private fun WidgetToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF38BDF8),
                checkedTrackColor = Color(0x5538BDF8)
            )
        )
    }
}
