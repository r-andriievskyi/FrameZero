package com.frame.zero.design.tokens

/**
 * Single source of truth for design tokens, read by Compose (`composeApp/shared/design_system`
 * adapts these to `Color`/`Dp`/`TextStyle`) and by SwiftUI through the iOS framework.
 */
object DesignTokens {
  val light = ColorTokens(
    background = Neutral050,
    surfaceElevated = Neutral100L,
    navBackground = Neutral050,
    inputBackground = Neutral100L,
    cardBackground = Neutral100L,
    border = Neutral200L,
    cardBorder = Neutral200L,
    textPrimary = Neutral900L,
    textSecondary = Neutral600L,
    textMuted = Neutral500L,
    textOnAccent = White,
    accent = Beacon500,
    accentDim = Beacon600,
    accentSurface = BeaconSoftL,
    accentText = BeaconTextL,
    successSurface = TealSoftL,
    successText = TealTextL,
    warningSurface = AmberSoftL,
    warningText = AmberTextL,
    errorSurface = RoseSoftL,
    errorText = RoseTextL,
    priorityHighSurface = RoseSoftL,
    priorityHighText = RoseTextL,
    priorityMedSurface = AmberSoftL,
    priorityMedText = AmberTextL,
    priorityLowSurface = TealSoftL,
    priorityLowText = TealTextL,
    developmentSurface = SkySoftL,
    developmentText = SkyTextL,
    preProductionSurface = AmberSoftL,
    preProductionText = AmberTextL,
    productionSurface = TealSoftL,
    productionText = TealTextL,
    postProductionSurface = BeaconSoftL,
    postProductionText = Beacon500,
    distributionSurface = RoseSoftL,
    distributionText = RoseTextL
  )

  val dark = ColorTokens(
    background = Neutral900,
    surfaceElevated = Neutral750,
    navBackground = Neutral850,
    inputBackground = Neutral800,
    cardBackground = Neutral780,
    border = Neutral600,
    cardBorder = Neutral700,
    textPrimary = Neutral100,
    textSecondary = Neutral300,
    textMuted = Neutral400,
    textOnAccent = White,
    accent = Beacon500,
    accentDim = Beacon600,
    accentSurface = BeaconSoftD,
    accentText = BeaconTextD,
    successSurface = TealSoftD,
    successText = TealTextD,
    warningSurface = AmberSoftD,
    warningText = AmberTextD,
    errorSurface = RoseSoftD,
    errorText = RoseTextD,
    priorityHighSurface = RoseSoftD,
    priorityHighText = RoseTextD,
    priorityMedSurface = AmberSoftD,
    priorityMedText = AmberTextD,
    priorityLowSurface = TealSoftD,
    priorityLowText = TealTextD,
    developmentSurface = SkySoftD,
    developmentText = SkyTextD,
    preProductionSurface = AmberSoftD,
    preProductionText = AmberTextD,
    productionSurface = TealSoftD,
    productionText = TealTextD,
    postProductionSurface = BeaconSoftD,
    postProductionText = BeaconTextD,
    distributionSurface = RoseSoftD,
    distributionText = RoseTextD
  )

  val typography = TypographyTokens(
    displayLarge = TextStyleToken(FontFamilyToken.SANS, weight = 700, size = 26f, letterSpacing = -0.5f),
    displayMedium = TextStyleToken(FontFamilyToken.SANS, weight = 700, size = 24f, letterSpacing = -0.5f),
    titleExtraLarge = TextStyleToken(FontFamilyToken.SANS, weight = 700, size = 17f, letterSpacing = -0.3f),
    titleLarge = TextStyleToken(FontFamilyToken.SANS, weight = 700, size = 15f, letterSpacing = -0.3f),
    titleMedium = TextStyleToken(FontFamilyToken.SANS, weight = 600, size = 13f),
    titleSmall = TextStyleToken(FontFamilyToken.SANS, weight = 500, size = 12f),
    bodyLarge = TextStyleToken(FontFamilyToken.SANS, weight = 400, size = 15f, lineHeight = 22f),
    bodyMedium = TextStyleToken(FontFamilyToken.SANS, weight = 400, size = 14f, lineHeight = 20f),
    bodySmall = TextStyleToken(FontFamilyToken.SANS, weight = 400, size = 13f, lineHeight = 18f),
    labelLarge = TextStyleToken(FontFamilyToken.SANS, weight = 600, size = 16f),
    labelMedium = TextStyleToken(FontFamilyToken.SANS, weight = 600, size = 13f),
    labelSmall = TextStyleToken(FontFamilyToken.SANS, weight = 600, size = 11f, letterSpacing = 0.7f),
    caption = TextStyleToken(FontFamilyToken.SANS, weight = 500, size = 10f),
    monoMedium = TextStyleToken(FontFamilyToken.MONO, weight = 400, size = 13f),
    monoSmall = TextStyleToken(FontFamilyToken.MONO, weight = 400, size = 11f)
  )

  val spacing = SpacingTokens(
    space2 = 2f,
    space4 = 4f,
    space8 = 8f,
    space12 = 12f,
    space16 = 16f,
    space24 = 24f,
    space32 = 32f
  )

  val radius = RadiusTokens(
    radius4 = 4f,
    radius8 = 8f,
    radius14 = 14f,
    radius16 = 16f,
    radiusMax = 100f
  )

  val border = BorderTokens(hairline = 1f)

  val motion = MotionTokens(
    durationFast = 150,
    durationMedium = 250,
    durationSlow = 400,
    durationLoop = 900,
    easingStandard = CubicBezier(0.2f, 0.0f, 0.0f, 1.0f),
    easingEmphasized = CubicBezier(0.2f, 0.0f, 0.0f, 1.0f),
    easingDecelerate = CubicBezier(0.0f, 0.0f, 0.2f, 1.0f),
    easingAccelerate = CubicBezier(0.4f, 0.0f, 1.0f, 1.0f),
    easingLinear = CubicBezier.Linear
  )
}
