package com.frame.zero.design.tokens

enum class FontFamilyToken { SANS, MONO }

/**
 * A text style in platform-neutral units: [weight] on the 100–900 scale, sizes in sp/pt.
 * `null` [letterSpacing]/[lineHeight] = the platform default.
 */
data class TextStyleToken(
  val fontFamily: FontFamilyToken,
  val weight: Int,
  val size: Float,
  val letterSpacing: Float? = null,
  val lineHeight: Float? = null
)

data class TypographyTokens(
  val displayLarge: TextStyleToken,
  val displayMedium: TextStyleToken,
  val titleExtraLarge: TextStyleToken,
  val titleLarge: TextStyleToken,
  val titleMedium: TextStyleToken,
  val titleSmall: TextStyleToken,
  val bodyLarge: TextStyleToken,
  val bodyMedium: TextStyleToken,
  val bodySmall: TextStyleToken,
  val labelLarge: TextStyleToken,
  val labelMedium: TextStyleToken,
  val labelSmall: TextStyleToken,
  val caption: TextStyleToken,
  val monoMedium: TextStyleToken,
  val monoSmall: TextStyleToken
)
