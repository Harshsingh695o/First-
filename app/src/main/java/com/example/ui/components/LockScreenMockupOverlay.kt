package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockStyle
import com.example.data.WallpaperConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LockScreenMockupOverlay(
    config: WallpaperConfig,
    onClockClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = Date()
            delay(1000L)
        }
    }

    // Battery status
    var batteryPct by remember { mutableIntStateOf(85) }
    var isCharging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPct = ((level / scale.toFloat()) * 100).toInt()
            }
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (_: Exception) {}
    }

    // Interactive Widget States
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraFlashTrigger by remember { mutableStateOf(false) }
    var isMusicPlaying by remember { mutableStateOf(true) }
    var isCelsius by remember { mutableStateOf(true) }
    var currentSongIndex by remember { mutableIntStateOf(0) }

    val songs = listOf(
        Pair("Midnight Mirage", "Lumina Wave"),
        Pair("Neon Starlight", "Cyber Chill"),
        Pair("Quantum Horizon", "Aether Echo"),
        Pair("Solar Flare", "Vapor Drift")
    )

    val timePattern = if (config.use24Hour) {
        if (config.showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (config.showSeconds) "hh:mm:ss" else "hh:mm"
    }
    val timeFormatter = remember(timePattern) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }

    val timeString = timeFormatter.format(currentTime)
    val dateString = dateFormatter.format(currentTime)

    val clockColor = try {
        Color(android.graphics.Color.parseColor(config.clockColorHex))
    } catch (_: Exception) {
        Color(0xFF38BDF8)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Top Lock Icon & Header status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Weather Pill (Interactive tap to switch °C / °F)
            if (config.showWeatherWidget) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isCelsius = !isCelsius }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("lock_weather_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCelsius) "24°C" else "75°F",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Lock Icon in center top
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Device Locked",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )

            // Battery Pill (Interactive)
            if (config.showBatteryWidget) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("lock_battery_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$batteryPct%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }

        // Dynamic Clock Element (Positioned according to clockYOffset)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = (config.clockYOffset * 480).dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClockClicked() }
                .testTag("lock_clock_element"),
            horizontalAlignment = when (config.clockXAlign) {
                "LEFT" -> Alignment.Start
                "RIGHT" -> Alignment.End
                else -> Alignment.CenterHorizontally
            }
        ) {
            when (config.clockStyle) {
                ClockStyle.CYBER_PULSE -> {
                    Text(
                        text = timeString,
                        color = clockColor,
                        fontSize = (54 * config.clockSize).sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .shadow(
                                elevation = if (config.clockGlow) 18.dp else 0.dp,
                                spotColor = clockColor,
                                ambientColor = clockColor
                            )
                    )
                    if (config.showDate) {
                        Text(
                            text = dateString.uppercase(),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                ClockStyle.MINIMAL_BOLD -> {
                    Text(
                        text = timeString,
                        color = Color.White,
                        fontSize = (68 * config.clockSize).sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    if (config.showDate) {
                        Text(
                            text = dateString,
                            color = clockColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                ClockStyle.NEO_DIGITAL -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x550A0E1A))
                            .border(1.5.dp, clockColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = timeString,
                                color = clockColor,
                                fontSize = (48 * config.clockSize).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (config.showDate) {
                                Text(
                                    text = dateString,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                ClockStyle.RETRO_FLIP -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        timeString.split(":").forEach { segment ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x801E293B))
                                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = segment,
                                    color = Color.White,
                                    fontSize = (38 * config.clockSize).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (config.showDate) {
                        Text(
                            text = dateString,
                            color = clockColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                ClockStyle.ELEGANT_SERIF -> {
                    Text(
                        text = timeString,
                        color = Color.White,
                        fontSize = (60 * config.clockSize).sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    if (config.showDate) {
                        Text(
                            text = dateString,
                            color = clockColor,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Interactive Music Player Card (Centered lower area)
        if (config.showMusicWidget) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 110.dp)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x600F172A))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                    .padding(14.dp)
                    .testTag("lock_music_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Album art thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFA855F7), Color(0xFF38BDF8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = songs[currentSongIndex].first,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = songs[currentSongIndex].second,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Music Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isMusicPlaying = !isMusicPlaying },
                            modifier = Modifier.size(36.dp).testTag("music_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isMusicPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                currentSongIndex = (currentSongIndex + 1) % songs.size
                            },
                            modifier = Modifier.size(36.dp).testTag("music_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next track",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Camera simulated flash overlay
        if (cameraFlashTrigger) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )
            LaunchedEffect(Unit) {
                delay(120L)
                cameraFlashTrigger = false
            }
        }

        // Bottom Quick Triggers (Torch & Camera) & Unlock Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch Trigger
                if (config.showTorchTrigger) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isTorchOn) Color(0xFFFBBF24) else Color(0x40000000))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable { isTorchOn = !isTorchOn }
                            .testTag("lock_torch_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight Toggle",
                            tint = if (isTorchOn) Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(52.dp))
                }

                // Center swipe affordance
                val infiniteTransition = rememberInfiniteTransition(label = "swipe")
                val arrowOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "arrow"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = arrowOffset.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Swipe to unlock",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Camera Trigger
                if (config.showCameraTrigger) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x40000000))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable { cameraFlashTrigger = true }
                            .testTag("lock_camera_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Shortcut",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(52.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation gesture bar
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.6f))
            )
        }
    }
}
