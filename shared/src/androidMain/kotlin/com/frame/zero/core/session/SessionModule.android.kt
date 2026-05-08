package com.frame.zero.core.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.mp.KoinPlatformTools
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE_ALIAS = "frame_zero_token_key"
private const val PREFS_NAME = "frame_zero_secure_prefs"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128

internal actual fun createTokenSettings(): Settings {
  val context = KoinPlatformTools.defaultContext().get().get<Context>()
  val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  val backing = SharedPreferencesSettings(prefs)
  return EncryptedStringSettings(backing)
}

private fun getOrCreateSecretKey(): SecretKey {
  val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
  if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
    return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
  }
  val spec = KeyGenParameterSpec.Builder(
    KEYSTORE_ALIAS,
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
  )
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    .setKeySize(256)
    .build()
  return KeyGenerator
    .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    .apply { init(spec) }
    .generateKey()
}

private fun encryptValue(plaintext: String): String {
  val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
  cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
  val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
  val encodedCiphertext = Base64.encodeToString(
    cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)),
    Base64.NO_WRAP
  )
  return "$encodedIv:$encodedCiphertext"
}

private fun decryptValue(stored: String): String? =
  runCatching {
    val separatorIndex = stored.indexOf(':')
    if (separatorIndex == -1) return null
    val iv = Base64.decode(stored.substring(0, separatorIndex), Base64.NO_WRAP)
    val ciphertext = Base64.decode(stored.substring(separatorIndex + 1), Base64.NO_WRAP)
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    cipher.init(
      Cipher.DECRYPT_MODE,
      getOrCreateSecretKey(),
      GCMParameterSpec(GCM_TAG_BITS, iv)
    )
    String(cipher.doFinal(ciphertext), Charsets.UTF_8)
  }.getOrNull()

/**
 * Delegates everything to [backing] except string operations, which are
 * transparently encrypted/decrypted with an AES-256-GCM key from Android Keystore.
 */
private class EncryptedStringSettings(
  private val backing: Settings
) : Settings by backing {
  override fun putString(
    key: String,
    value: String
  ) = backing.putString(key, encryptValue(value))

  override fun getString(
    key: String,
    defaultValue: String
  ): String = backing.getStringOrNull(key)?.let { decryptValue(it) } ?: defaultValue

  override fun getStringOrNull(key: String): String? = backing.getStringOrNull(key)?.let { decryptValue(it) }
}
