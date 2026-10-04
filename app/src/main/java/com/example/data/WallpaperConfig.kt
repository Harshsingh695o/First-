package com.example.data

enum class ScalingMode(val label: String) {
    CROP_FILL("Fill & Crop"),
    FIT_CENTER("Fit to Screen"),
    STRETCH("Stretch to Fill")
}

enum class VideoFilterType(val label: String, val colorTint: Long?) {
    NONE("Normal", null),
    CYBER_NEON("Cyber Neon", 0x33A855F7),
    SUNSET_AMBER("Sunset Amber", 0x33F59E0B),
    MIDNIGHT_BLUE("Midnight Blue", 0x3338BDF8),
    EMERALD("Matrix Emerald", 0x3310B981),
    MONOCHROME("Noir Cinema", 0x44000000)
}

enum class TouchEffectType(val label: String, val description: String) {
    RIPPLE("Liquid Ripples", "Fluid expanding circular wavelets on tap"),
    PARTICLES("Neon Particles", "Floating cosmic particles burst at touch point"),
    SPARKLE("Electric Sparks", "Bright energetic sparks that radiate outward"),
    PULSE("Glow Pulse", "Smooth radial illumination under finger"),
    NONE("Off", "No visual touch feedback")
}

enum class DoubleTapAction(val label: String) {
    PAUSE_PLAY("Play / Pause Video"),
    MUTE_UNMUTE("Mute / Unmute Audio"),
    NEXT_PRESET("Cycle to Next Dynamic Preset"),
    BURST("Firework Touch Burst"),
    NONE("None")
}

enum class ScreenType(val label: String) {
    LOCK_SCREEN("Lock Screen"),
    HOME_SCREEN("Home Screen")
}

enum class ClockStyle(val label: String) {
    CYBER_PULSE("Cyber Pulse"),
    MINIMAL_BOLD("Minimalist Bold"),
    NEO_DIGITAL("Neo Digital"),
    RETRO_FLIP("Retro Flip"),
    ELEGANT_SERIF("Elegant Serif")
}

data class VideoPreset(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val tag: String
)

data class WallpaperConfig(
    val selectedPresetId: String = "synthwave_horizon",
    val customVideoPath: String? = null,
    val customVideoUri: String? = null,
    val customVideoTitle: String? = null,
    val isMuted: Boolean = true,
    val volume: Float = 0.5f,
    val playbackSpeed: Float = 1.0f,
    val scalingMode: ScalingMode = ScalingMode.CROP_FILL,
    val filterType: VideoFilterType = VideoFilterType.NONE,
    val filterIntensity: Float = 0.6f,

    // Interactive elements
    val touchEffect: TouchEffectType = TouchEffectType.RIPPLE,
    val touchHaptics: Boolean = true,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.PAUSE_PLAY,

    // Screen Layout customizer
    val activePreviewScreen: ScreenType = ScreenType.LOCK_SCREEN,
    val clockStyle: ClockStyle = ClockStyle.CYBER_PULSE,
    val clockSize: Float = 1.0f,
    val clockYOffset: Float = 0.18f, // 0.05 to 0.70 of screen height
    val clockXAlign: String = "CENTER",
    val clockColorHex: String = "#38BDF8",
    val clockGlow: Boolean = true,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val use24Hour: Boolean = false,

    // Lock screen elements
    val showBatteryWidget: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showMusicWidget: Boolean = true,
    val showTorchTrigger: Boolean = true,
    val showCameraTrigger: Boolean = true,

    // Home screen layout simulator elements
    val showAppIcons: Boolean = true,
    val gridRows: Int = 5,
    val gridCols: Int = 4,
    val showSearchBar: Boolean = true,
    val showDock: Boolean = true,
    val iconStyle: String = "NEON", // "NEON", "MINIMAL", "GLASS"
    val parallaxEnabled: Boolean = true
)
