package com.frame.zero.core.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.mp.KoinPlatformTools

private const val PREFS_NAME = "frame_zero_secure_prefs"

internal actual fun createTokenSettings(): Settings {
  val context = KoinPlatformTools.defaultContext().get().get<Context>()
  val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
  val prefs =
    EncryptedSharedPreferences.create(
      context,
      PREFS_NAME,
      masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  return SharedPreferencesSettings(prefs)
}
