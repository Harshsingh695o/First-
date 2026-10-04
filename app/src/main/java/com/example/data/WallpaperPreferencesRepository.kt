package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperPreferencesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<WallpaperConfig> = _configFlow.asStateFlow()

    fun getConfig(): WallpaperConfig = _configFlow.value

    fun updateConfig(update: (WallpaperConfig) -> WallpaperConfig) {
        val newConfig = update(_configFlow.value)
        _configFlow.value = newConfig
        saveConfig(newConfig)
    }

    private fun loadConfig(): WallpaperConfig {
        return WallpaperConfig(
            selectedPresetId = prefs.getString(KEY_PRESET_ID, "synthwave_horizon") ?: "synthwave_horizon",
            customVideoPath = prefs.getString(KEY_CUSTOM_PATH, null),
            customVideoUri = prefs.getString(KEY_CUSTOM_URI, null),
            customVideoTitle = prefs.getString(KEY_CUSTOM_TITLE, null),
            isMuted = prefs.getBoolean(KEY_IS_MUTED, true),
            volume = prefs.getFloat(KEY_VOLUME, 0.5f),
            playbackSpeed = prefs.getFloat(KEY_SPEED, 1.0f),
            scalingMode = try {
                ScalingMode.valueOf(prefs.getString(KEY_SCALING_MODE, ScalingMode.CROP_FILL.name) ?: ScalingMode.CROP_FILL.name)
            } catch (e: Exception) {
                ScalingMode.CROP_FILL
            },
            filterType = try {
                VideoFilterType.valueOf(prefs.getString(KEY_FILTER_TYPE, VideoFilterType.NONE.name) ?: VideoFilterType.NONE.name)
            } catch (e: Exception) {
                VideoFilterType.NONE
            },
            filterIntensity = prefs.getFloat(KEY_FILTER_INTENSITY, 0.6f),
            touchEffect = try {
                TouchEffectType.valueOf(prefs.getString(KEY_TOUCH_EFFECT, TouchEffectType.RIPPLE.name) ?: TouchEffectType.RIPPLE.name)
            } catch (e: Exception) {
                TouchEffectType.RIPPLE
            },
            touchHaptics = prefs.getBoolean(KEY_TOUCH_HAPTICS, true),
            doubleTapAction = try {
                DoubleTapAction.valueOf(prefs.getString(KEY_DOUBLE_TAP, DoubleTapAction.PAUSE_PLAY.name) ?: DoubleTapAction.PAUSE_PLAY.name)
            } catch (e: Exception) {
                DoubleTapAction.PAUSE_PLAY
            },
            activePreviewScreen = try {
                ScreenType.valueOf(prefs.getString(KEY_SCREEN_TYPE, ScreenType.LOCK_SCREEN.name) ?: ScreenType.LOCK_SCREEN.name)
            } catch (e: Exception) {
                ScreenType.LOCK_SCREEN
            },
            clockStyle = try {
                ClockStyle.valueOf(prefs.getString(KEY_CLOCK_STYLE, ClockStyle.CYBER_PULSE.name) ?: ClockStyle.CYBER_PULSE.name)
            } catch (e: Exception) {
                ClockStyle.CYBER_PULSE
            },
            clockSize = prefs.getFloat(KEY_CLOCK_SIZE, 1.0f),
            clockYOffset = prefs.getFloat(KEY_CLOCK_Y_OFFSET, 0.18f),
            clockXAlign = prefs.getString(KEY_CLOCK_X_ALIGN, "CENTER") ?: "CENTER",
            clockColorHex = prefs.getString(KEY_CLOCK_COLOR, "#38BDF8") ?: "#38BDF8",
            clockGlow = prefs.getBoolean(KEY_CLOCK_GLOW, true),
            showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, false),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            use24Hour = prefs.getBoolean(KEY_USE_24_HOUR, false),
            showBatteryWidget = prefs.getBoolean(KEY_SHOW_BATTERY, true),
            showWeatherWidget = prefs.getBoolean(KEY_SHOW_WEATHER, true),
            showMusicWidget = prefs.getBoolean(KEY_SHOW_MUSIC, true),
            showTorchTrigger = prefs.getBoolean(KEY_SHOW_TORCH, true),
            showCameraTrigger = prefs.getBoolean(KEY_SHOW_CAMERA, true),
            showAppIcons = prefs.getBoolean(KEY_SHOW_APP_ICONS, true),
            gridRows = prefs.getInt(KEY_GRID_ROWS, 5),
            gridCols = prefs.getInt(KEY_GRID_COLS, 4),
            showSearchBar = prefs.getBoolean(KEY_SHOW_SEARCH_BAR, true),
            showDock = prefs.getBoolean(KEY_SHOW_DOCK, true),
            iconStyle = prefs.getString(KEY_ICON_STYLE, "NEON") ?: "NEON",
            parallaxEnabled = prefs.getBoolean(KEY_PARALLAX, true)
        )
    }

    private fun saveConfig(config: WallpaperConfig) {
        prefs.edit()
            .putString(KEY_PRESET_ID, config.selectedPresetId)
            .putString(KEY_CUSTOM_PATH, config.customVideoPath)
            .putString(KEY_CUSTOM_URI, config.customVideoUri)
            .putString(KEY_CUSTOM_TITLE, config.customVideoTitle)
            .putBoolean(KEY_IS_MUTED, config.isMuted)
            .putFloat(KEY_VOLUME, config.volume)
            .putFloat(KEY_SPEED, config.playbackSpeed)
            .putString(KEY_SCALING_MODE, config.scalingMode.name)
            .putString(KEY_FILTER_TYPE, config.filterType.name)
            .putFloat(KEY_FILTER_INTENSITY, config.filterIntensity)
            .putString(KEY_TOUCH_EFFECT, config.touchEffect.name)
            .putBoolean(KEY_TOUCH_HAPTICS, config.touchHaptics)
            .putString(KEY_DOUBLE_TAP, config.doubleTapAction.name)
            .putString(KEY_SCREEN_TYPE, config.activePreviewScreen.name)
            .putString(KEY_CLOCK_STYLE, config.clockStyle.name)
            .putFloat(KEY_CLOCK_SIZE, config.clockSize)
            .putFloat(KEY_CLOCK_Y_OFFSET, config.clockYOffset)
            .putString(KEY_CLOCK_X_ALIGN, config.clockXAlign)
            .putString(KEY_CLOCK_COLOR, config.clockColorHex)
            .putBoolean(KEY_CLOCK_GLOW, config.clockGlow)
            .putBoolean(KEY_SHOW_SECONDS, config.showSeconds)
            .putBoolean(KEY_SHOW_DATE, config.showDate)
            .putBoolean(KEY_USE_24_HOUR, config.use24Hour)
            .putBoolean(KEY_SHOW_BATTERY, config.showBatteryWidget)
            .putBoolean(KEY_SHOW_WEATHER, config.showWeatherWidget)
            .putBoolean(KEY_SHOW_MUSIC, config.showMusicWidget)
            .putBoolean(KEY_SHOW_TORCH, config.showTorchTrigger)
            .putBoolean(KEY_SHOW_CAMERA, config.showCameraTrigger)
            .putBoolean(KEY_SHOW_APP_ICONS, config.showAppIcons)
            .putInt(KEY_GRID_ROWS, config.gridRows)
            .putInt(KEY_GRID_COLS, config.gridCols)
            .putBoolean(KEY_SHOW_SEARCH_BAR, config.showSearchBar)
            .putBoolean(KEY_SHOW_DOCK, config.showDock)
            .putString(KEY_ICON_STYLE, config.iconStyle)
            .putBoolean(KEY_PARALLAX, config.parallaxEnabled)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "lumina_wallpaper_prefs"

        private const val KEY_PRESET_ID = "preset_id"
        private const val KEY_CUSTOM_PATH = "custom_path"
        private const val KEY_CUSTOM_URI = "custom_uri"
        private const val KEY_CUSTOM_TITLE = "custom_title"
        private const val KEY_IS_MUTED = "is_muted"
        private const val KEY_VOLUME = "volume"
        private const val KEY_SPEED = "speed"
        private const val KEY_SCALING_MODE = "scaling_mode"
        private const val KEY_FILTER_TYPE = "filter_type"
        private const val KEY_FILTER_INTENSITY = "filter_intensity"

        private const val KEY_TOUCH_EFFECT = "touch_effect"
        private const val KEY_TOUCH_HAPTICS = "touch_haptics"
        private const val KEY_DOUBLE_TAP = "double_tap"

        private const val KEY_SCREEN_TYPE = "screen_type"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_CLOCK_SIZE = "clock_size"
        private const val KEY_CLOCK_Y_OFFSET = "clock_y_offset"
        private const val KEY_CLOCK_X_ALIGN = "clock_x_align"
        private const val KEY_CLOCK_COLOR = "clock_color"
        private const val KEY_CLOCK_GLOW = "clock_glow"
        private const val KEY_SHOW_SECONDS = "show_seconds"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_USE_24_HOUR = "use_24_hour"

        private const val KEY_SHOW_BATTERY = "show_battery"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_SHOW_MUSIC = "show_music"
        private const val KEY_SHOW_TORCH = "show_torch"
        private const val KEY_SHOW_CAMERA = "show_camera"

        private const val KEY_SHOW_APP_ICONS = "show_app_icons"
        private const val KEY_GRID_ROWS = "grid_rows"
        private const val KEY_GRID_COLS = "grid_cols"
        private const val KEY_SHOW_SEARCH_BAR = "show_search_bar"
        private const val KEY_SHOW_DOCK = "show_dock"
        private const val KEY_ICON_STYLE = "icon_style"
        private const val KEY_PARALLAX = "parallax"

        @Volatile
        private var INSTANCE: WallpaperPreferencesRepository? = null

        fun getInstance(context: Context): WallpaperPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WallpaperPreferencesRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
