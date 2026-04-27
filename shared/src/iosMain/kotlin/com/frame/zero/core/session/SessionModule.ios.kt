package com.frame.zero.core.session

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

private const val KEYCHAIN_SERVICE = "com.frame.zero.tokens"

@OptIn(ExperimentalSettingsImplementation::class)
internal actual fun createTokenSettings(): Settings = KeychainSettings(service = KEYCHAIN_SERVICE)
