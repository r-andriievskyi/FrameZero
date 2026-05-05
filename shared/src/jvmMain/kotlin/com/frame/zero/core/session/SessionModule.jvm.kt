package com.frame.zero.core.session

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

private const val PREFS_NODE = "com/frame/zero/tokens"

internal actual fun createTokenSettings(): Settings = PreferencesSettings(Preferences.userRoot().node(PREFS_NODE))
