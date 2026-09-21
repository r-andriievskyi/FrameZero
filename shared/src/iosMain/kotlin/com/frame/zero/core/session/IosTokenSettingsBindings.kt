package com.frame.zero.core.session

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

private const val KEYCHAIN_SERVICE = "com.frame.zero.tokens"

/** Token storage for iOS: the system Keychain. */
@ContributesTo(AppScope::class)
@BindingContainer
object IosTokenSettingsBindings {
  @Provides
  @SingleIn(AppScope::class)
  @OptIn(ExperimentalSettingsImplementation::class)
  fun tokenSettings(): Settings = KeychainSettings(service = KEYCHAIN_SERVICE)
}
