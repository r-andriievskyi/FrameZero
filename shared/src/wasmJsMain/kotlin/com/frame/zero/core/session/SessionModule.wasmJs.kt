package com.frame.zero.core.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

internal actual fun createTokenSettings(): Settings = StorageSettings()
