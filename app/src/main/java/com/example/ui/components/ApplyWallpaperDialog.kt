package com.example.ui.components

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.wallpaper.DynamicVideoWallpaperService

@Composable
fun ApplyWallpaperDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun launchSystemWallpaperSetter() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, DynamicVideoWallpaperService::class.java)
                )
            }
            context.startActivity(intent)
            onDismiss()
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                context.startActivity(fallbackIntent)
                onDismiss()
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not launch wallpaper picker directly. Check device Settings > Wallpaper.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131A29),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier.fillMaxWidth().testTag("apply_wallpaper_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Apply Dynamic Wallpaper",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Steps Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x331E293B))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    StepItem(step = "1", title = "Launch System Wallpaper", desc = "Tap the button below to open Android Live Wallpaper preview.")
                    Spacer(modifier = Modifier.height(10.dp))
                    StepItem(step = "2", title = "Select Target Screen", desc = "Choose 'Set wallpaper' and pick 'Home screen' or 'Home and lock screens'.")
                    Spacer(modifier = Modifier.height(10.dp))
                    StepItem(step = "3", title = "Interactive Live Experience", desc = "Enjoy video loops, touch ripples, and interactive gestures on your device!")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Battery & Performance Guarantee
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2210B981))
                        .border(1.dp, Color(0x4410B981), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Battery-Optimized Engine",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Video auto-pauses when apps are open or screen is locked. 0% GPU drain in background.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary CTA Button
                Button(
                    onClick = { launchSystemWallpaperSetter() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("apply_as_live_wallpaper_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set as System Wallpaper",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(step: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
        }
    }
}
