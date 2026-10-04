package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WallpaperConfig
import com.example.data.local.SavedVideoEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoLibraryScreen(
    videos: List<SavedVideoEntity>,
    activeConfig: WallpaperConfig,
    isImporting: Boolean,
    onUploadVideo: (Uri) -> Unit,
    onSelectAsWallpaper: (SavedVideoEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onRenameVideo: (Long, String) -> Unit,
    onDeleteVideo: (SavedVideoEntity) -> Unit,
    onPreseedPresets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, FAVORITES, UPLOADS, PRESETS

    // Video to preview in popup modal
    var previewingVideo by remember { mutableStateOf<SavedVideoEntity?>(null) }
    // Video to rename
    var renamingVideo by remember { mutableStateOf<SavedVideoEntity?>(null) }
    // Video to delete
    var deletingVideo by remember { mutableStateOf<SavedVideoEntity?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadVideo(uri)
        }
    }

    val filteredVideos = when (selectedFilter) {
        "FAVORITES" -> videos.filter { it.isFavorite }
        "UPLOADS" -> videos.filter { it.source == "USER_UPLOAD" }
        "PRESETS" -> videos.filter { it.source == "PRESET" }
        else -> videos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Hero Header & Upload Button
        HeroUploadHeader(
            isImporting = isImporting,
            onUploadClick = {
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${videos.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF38BDF8),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_all_videos")
            )

            FilterChip(
                selected = selectedFilter == "FAVORITES",
                onClick = { selectedFilter = "FAVORITES" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Favorites (${videos.count { it.isFavorite }})", fontSize = 12.sp)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFBBF24),
                    selectedLabelColor = Color.Black,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_favorite_videos")
            )

            FilterChip(
                selected = selectedFilter == "UPLOADS",
                onClick = { selectedFilter = "UPLOADS" },
                label = { Text("Uploaded (${videos.count { it.source == "USER_UPLOAD" }})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA855F7),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0x331E293B),
                    labelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_uploads_videos")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Video Items List or Empty State
        if (filteredVideos.isEmpty()) {
            EmptyLibraryState(
                filter = selectedFilter,
                onUploadClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onLoadPresetsClick = onPreseedPresets
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_library_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredVideos, key = { it.id }) { video ->
                    val isActive = activeConfig.customVideoPath == video.filePath

                    VideoItemCard(
                        video = video,
                        isActive = isActive,
                        onSelectAsWallpaper = { onSelectAsWallpaper(video) },
                        onPreviewClick = { previewingVideo = video },
                        onToggleFavorite = { onToggleFavorite(video.id, !video.isFavorite) },
                        onRenameClick = { renamingVideo = video },
                        onDeleteClick = { deletingVideo = video }
                    )
                }
            }
        }
    }

    // Modal: Preview Video
    previewingVideo?.let { video ->
        VideoPreviewModal(
            video = video,
            isActive = activeConfig.customVideoPath == video.filePath,
            onSetWallpaper = {
                onSelectAsWallpaper(video)
                previewingVideo = null
            },
            onDismiss = { previewingVideo = null }
        )
    }

    // Dialog: Rename Video
    renamingVideo?.let { video ->
        RenameVideoDialog(
            currentTitle = video.title,
            onConfirm = { newTitle ->
                onRenameVideo(video.id, newTitle)
                renamingVideo = null
            },
            onDismiss = { renamingVideo = null }
        )
    }

    // Dialog: Delete Video Confirmation
    deletingVideo?.let { video ->
        AlertDialog(
            onDismissRequest = { deletingVideo = null },
            title = { Text("Delete MP4 Video", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete \"${video.title}\" from your local video library? The video file will be permanently removed from app storage.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVideo(video)
                        deletingVideo = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVideo = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = Color(0xFF131A29),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun HeroUploadHeader(
    isImporting: Boolean,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.5.dp, Color(0x3338BDF8), RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x331E293B)
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0x3038BDF8), Color(0x20A855F7), Color(0x10000000))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stored MP4 Wallpapers",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload MP4 clips to store in app and apply as dynamic looping wallpaper.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Button(
                    onClick = onUploadClick,
                    enabled = !isImporting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("upload_mp4_library_button")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importing...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upload MP4",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoItemCard(
    video: SavedVideoEntity,
    isActive: Boolean,
    onSelectAsWallpaper: () -> Unit,
    onPreviewClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color(0xFF38BDF8) else Color(0x22FFFFFF),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A29)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Box with play overlay
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 104.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onPreviewClick() }
                        .testTag("video_thumbnail_${video.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    val thumbBitmap = remember(video.thumbnailPath) {
                        if (!video.thumbnailPath.isNullOrEmpty()) {
                            val f = File(video.thumbnailPath)
                            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                        } else null
                    }

                    if (thumbBitmap != null) {
                        Image(
                            bitmap = thumbBitmap.asImageBitmap(),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback gradient aesthetic preview
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF311042), Color(0xFF0F172A)))
                                )
                        )
                    }

                    // Play overlay pill
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Preview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Duration badge in bottom corner
                    if (video.durationMs > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xB3000000))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatDuration(video.durationMs),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Metadata Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Favorite Icon Button
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(28.dp).testTag("fav_button_${video.id}")
                        ) {
                            Icon(
                                imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (video.isFavorite) Color(0xFFF43F5E) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badges: Size, Resolution, Source
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgePill(text = formatFileSize(video.fileSizeBytes), color = Color(0xFF38BDF8))
                        if (video.width > 0 && video.height > 0) {
                            BadgePill(text = "${video.width}×${video.height}", color = Color(0xFFA855F7))
                        }
                        BadgePill(
                            text = if (video.source == "PRESET") "Preset" else "Uploaded",
                            color = if (video.source == "PRESET") Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Wallpaper Pill indicator
                    if (isActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3338BDF8))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active Dynamic Wallpaper",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onSelectAsWallpaper,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("apply_video_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Set as Dynamic Wallpaper",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Rename & Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRenameClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rename", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onDeleteClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("delete_video_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFF43F5E), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyLibraryState(
    filter: String,
    onUploadClick: () -> Unit,
    onLoadPresetsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0x2238BDF8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (filter == "FAVORITES") "No Favorite Wallpapers Yet" else "No MP4 Videos In Library",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (filter == "FAVORITES") "Star any video from the list to add it to your quick favorites."
                else "Upload MP4 videos from your phone gallery or storage to build your dynamic video wallpaper collection.",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload MP4", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onLoadPresetsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33A855F7)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Presets", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VideoPreviewModal(
    video: SavedVideoEntity,
    isActive: Boolean,
    onSetWallpaper: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatDuration(video.durationMs)} • ${formatFileSize(video.fileSizeBytes)}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color(0xFF38BDF8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Preview Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, Color(0x4038BDF8), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val thumbBitmap = remember(video.thumbnailPath) {
                        if (!video.thumbnailPath.isNullOrEmpty()) {
                            val f = File(video.thumbnailPath)
                            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                        } else null
                    }

                    if (thumbBitmap != null) {
                        Image(
                            bitmap = thumbBitmap.asImageBitmap(),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Play overlay
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onSetWallpaper,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Currently Active Wallpaper" else "Use as Dynamic Wallpaper",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameVideoDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Wallpaper", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter a display name for this MP4 wallpaper:", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0x40FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF131A29),
        shape = RoundedCornerShape(20.dp)
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.getDefault(), "%.1f MB", mb)
    } else {
        String.format(Locale.getDefault(), "%.0f KB", kb)
    }
}
