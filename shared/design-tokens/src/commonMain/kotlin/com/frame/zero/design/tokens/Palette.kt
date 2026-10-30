package com.frame.zero.design.tokens

// Raw ARGB palette. Only DesignTokens reads these; UI code uses the semantic ColorTokens roles.

internal const val White = 0xFFFFFFFF

// Neutral scale (blue-tinted, hue 250 — Beacon)
internal const val Neutral900 = 0xFF0B0D14
internal const val Neutral850 = 0xFF0F1119
internal const val Neutral800 = 0xFF161922
internal const val Neutral780 = 0xFF141823
internal const val Neutral750 = 0xFF191D26
internal const val Neutral700 = 0xFF1E232E
internal const val Neutral600 = 0xFF20242F
internal const val Neutral400 = 0xFF767E8E // muted text on dark bg — 4.75:1 (WCAG AA)
internal const val Neutral300 = 0xFFA4ABB9
internal const val Neutral100 = 0xFFF0F2F7
internal const val Neutral050 = 0xFFF5F6FA
internal const val Neutral100L = 0xFFEAEDF5
internal const val Neutral200L = 0xFFCFD7E7
internal const val Neutral500L = 0xFF687085 // muted text on light bg — 4.58:1 (WCAG AA)
internal const val Neutral600L = 0xFF4D5468
internal const val Neutral900L = 0xFF0F1219

// Beacon accent (primary) — accessibility-tuned: oklch hue 250
internal const val Beacon500 = 0xFF2A6FDB // oklch(58% 0.20 250) — primary action (≥4.5:1 on white)
internal const val Beacon600 = 0xFF1652B8 // oklch(48% 0.20 250) — pressed / dim
internal const val BeaconSoftD = 0xFF131F38 // oklch(22% 0.08 250) — accent bg on dark
internal const val BeaconSoftL = 0xFFDCE6F6 // oklch(92% 0.05 250) — accent bg on light
internal const val BeaconTextD = 0xFF98BAF6 // oklch(84% 0.13 250) — accent text on dark (~12:1)
internal const val BeaconTextL = 0xFF1F63CF // accent text on light — 5.2:1 AA (Beacon500 is 4.4:1)

// Status — success (teal)
internal const val TealSoftD = 0xFF0E2B1C // oklch(20% 0.07 155)
internal const val TealTextD = 0xFF4DC88A // oklch(73% 0.12 155)
internal const val TealSoftL = 0xFFCCEEDD // oklch(91% 0.08 155)
internal const val TealTextL = 0xFF115E30 // oklch(33% 0.12 155)

// Status — warning / medium priority (amber)
internal const val AmberSoftD = 0xFF251C08 // oklch(19% 0.07 80)
internal const val AmberTextD = 0xFFCDB84A // oklch(78% 0.12 80)
internal const val AmberSoftL = 0xFFF5EFCA // oklch(93% 0.08 80)
internal const val AmberTextL = 0xFF5C490A // oklch(36% 0.12 80)

// Status — error / high priority (rose)
internal const val RoseSoftD = 0xFF2A1410 // oklch(20% 0.07 20)
internal const val RoseTextD = 0xFFDF8065 // oklch(72% 0.14 20)
internal const val RoseSoftL = 0xFFFAEDE9 // oklch(94% 0.07 20)
internal const val RoseTextL = 0xFF8B3A20 // oklch(38% 0.14 20)

// Development phase (sky)
internal const val SkySoftD = 0xFF0E1E2D // oklch(20% 0.06 230)
internal const val SkyTextD = 0xFF6FAEE8 // oklch(75% 0.12 230)
internal const val SkySoftL = 0xFFDDEAF7 // oklch(93% 0.05 230)
internal const val SkyTextL = 0xFF1B4A85 // oklch(36% 0.13 230)
