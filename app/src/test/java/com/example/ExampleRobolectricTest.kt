package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClockStyle
import com.example.data.DoubleTapAction
import com.example.data.ScreenType
import com.example.data.TouchEffectType
import com.example.data.WallpaperPreferencesRepository
import com.example.data.local.AppDatabase
import com.example.data.local.SavedVideoEntity
import com.example.video.VideoPresetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LuminaMotion", appName)
    }

    @Test
    fun `wallpaper repository saves and loads config properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = WallpaperPreferencesRepository.getInstance(context)

        repository.updateConfig {
            it.copy(
                selectedPresetId = "cosmic_nebula",
                clockStyle = ClockStyle.MINIMAL_BOLD,
                activePreviewScreen = ScreenType.HOME_SCREEN,
                touchEffect = TouchEffectType.PARTICLES,
                doubleTapAction = DoubleTapAction.NEXT_PRESET
            )
        }

        val config = repository.getConfig()
        assertEquals("cosmic_nebula", config.selectedPresetId)
        assertEquals(ClockStyle.MINIMAL_BOLD, config.clockStyle)
        assertEquals(ScreenType.HOME_SCREEN, config.activePreviewScreen)
        assertEquals(TouchEffectType.PARTICLES, config.touchEffect)
        assertEquals(DoubleTapAction.NEXT_PRESET, config.doubleTapAction)
    }

    @Test
    fun `preset manager contains required presets`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = VideoPresetManager.getInstance(context)
        assertTrue(manager.presets.isNotEmpty())
        assertNotNull(manager.presets.find { it.id == "synthwave_horizon" })
        assertNotNull(manager.presets.find { it.id == "cosmic_nebula" })
    }

    @Test
    fun `saved video dao inserts and queries videos`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val dao = db.savedVideoDao()

        val sampleVideo = SavedVideoEntity(
            title = "Cyber City Loop",
            filePath = "/fake/path/cyber.mp4",
            thumbnailPath = null,
            durationMs = 15000L,
            fileSizeBytes = 4500000L,
            width = 1080,
            height = 1920,
            isFavorite = true,
            source = "USER_UPLOAD"
        )

        val id = dao.insertVideo(sampleVideo)
        assertTrue(id > 0)

        val retrieved = dao.getVideoById(id)
        assertNotNull(retrieved)
        assertEquals("Cyber City Loop", retrieved?.title)
        assertTrue(retrieved?.isFavorite == true)

        dao.renameVideo(id, "Neon Metropolis")
        val updated = dao.getVideoById(id)
        assertEquals("Neon Metropolis", updated?.title)

        dao.deleteVideoById(id)
        val deleted = dao.getVideoById(id)
        assertEquals(null, deleted)
    }
}
