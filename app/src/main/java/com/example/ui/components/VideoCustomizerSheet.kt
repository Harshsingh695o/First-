package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScalingMode
import com.example.data.VideoFilterType
import com.example.data.WallpaperConfig
import com.example.data.repository.VideoLibraryRepository
import com.example.video.VideoPresetManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.roundToInt

@Composable
fun VideoCustomizerSheet(
    config: WallpaperConfig,
    onConfigChange: ((WallpaperConfig) -> WallpaperConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val presetManager = remember { VideoPresetManager.getInstance(context) }
    val libraryRepo = remember { VideoLibraryRepository.getInstance(context) }
    val presets = presetManager.presets

    // Android Photo/Video Picker (Zero permissions, modern Android standard!)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = libraryRepo.importVideo(uri)
                result.onSuccess { saved ->
                    onConfigChange {
                        it.copy(
                            customVideoPath = saved.filePath,
                            customVideoTitle = saved.title,
                            customVideoUri = null
                        )
                    }
                    Toast.makeText(context, "Saved \"${saved.title}\" to Video Library and set as wallpaper!", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Could not load video file. Please try another MP4.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section: Presets & Custom MP4 Video
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dynamic MP4 Library",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Custom MP4 Picker Button
            Button(
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("import_mp4_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Import MP4", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom imported video banner (if selected)
        if (!config.customVideoPath.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x40A855F7))
                    .border(1.5.dp, Color(0xFFA855F7), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Active Custom Video",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = config.customVideoTitle ?: "Custom Imported MP4",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            onConfigChange { it.copy(customVideoPath = null, customVideoTitle = null) }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset to Preset", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Preset cards horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = config.customVideoPath == null && config.selectedPresetId == preset.id

                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(preset.primaryColorHex).copy(alpha = if (isSelected) 0.5f else 0.25f),
                                    Color(preset.secondaryColorHex).copy(alpha = if (isSelected) 0.6f else 0.2f),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            onConfigChange {
                                it.copy(
                                    selectedPresetId = preset.id,
                                    customVideoPath = null,
                                    customVideoTitle = null
                                )
                            }
                        }
                        .padding(12.dp)
                        .testTag("preset_card_${preset.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x55000000))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = preset.category,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = preset.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = preset.description,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Playback Speed
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Motion Playback Speed: ${config.playbackSpeed}x",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
            speedOptions.forEach { spd ->
                FilterChip(
                    selected = config.playbackSpeed == spd,
                    onClick = { onConfigChange { it.copy(playbackSpeed = spd) } },
                    label = { Text("${spd}x", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF38BDF8),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("speed_chip_${spd}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Audio Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x331E293B))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (config.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (config.isMuted) Color(0xFF94A3B8) else Color(0xFF34D399),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (config.isMuted) "Wallpaper Audio Muted" else "Wallpaper Audio Active",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Play ambient video sound with live wallpaper",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = !config.isMuted,
                onCheckedChange = { isEnabled ->
                    onConfigChange { it.copy(isMuted = !isEnabled) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF34D399),
                    checkedTrackColor = Color(0x5534D399)
                ),
                modifier = Modifier.testTag("audio_mute_switch")
            )
        }

        if (!config.isMuted) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Volume", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(text = "${(config.volume * 100).roundToInt()}%", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = config.volume,
                    onValueChange = { vol -> onConfigChange { it.copy(volume = vol) } },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF34D399),
                        activeTrackColor = Color(0xFF34D399)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Video Aspect Ratio Scaling
        Text(
            text = "Video Scaling Mode",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScalingMode.values().forEach { mode ->
                FilterChip(
                    selected = config.scalingMode == mode,
                    onClick = { onConfigChange { it.copy(scalingMode = mode) } },
                    label = { Text(mode.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFA855F7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("scaling_chip_${mode.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Color & Atmosphere Filters
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.FilterVintage,
                contentDescription = null,
                tint = Color(0xFFF472B6),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Atmospheric Tint Filter",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VideoFilterType.values().forEach { filter ->
                FilterChip(
                    selected = config.filterType == filter,
                    onClick = { onConfigChange { it.copy(filterType = filter) } },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF472B6),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0x331E293B),
                        labelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }

        if (config.filterType != VideoFilterType.NONE) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Filter Intensity", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Text(text = "${(config.filterIntensity * 100).roundToInt()}%", color = Color(0xFFF472B6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = config.filterIntensity,
                onValueChange = { intensity -> onConfigChange { it.copy(filterIntensity = intensity) } },
                valueRange = 0.1f..0.9f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF472B6),
                    activeTrackColor = Color(0xFFF472B6)
                )
            )
        }
    }
}
