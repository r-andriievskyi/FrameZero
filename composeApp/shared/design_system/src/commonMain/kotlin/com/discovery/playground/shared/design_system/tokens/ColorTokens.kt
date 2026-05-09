package com.discovery.playground.shared.design_system.tokens

import androidx.compose.ui.graphics.Color

internal val TokenColorWhite = Color(0xFFFFFFFF)

// Neutral scale (blue-tinted, hue 250 — Beacon)
internal val TokenColorNeutral900 = Color(0xFF0B0D14)
internal val TokenColorNeutral850 = Color(0xFF0F1119)
internal val TokenColorNeutral800 = Color(0xFF161922)
internal val TokenColorNeutral780 = Color(0xFF141823)
internal val TokenColorNeutral750 = Color(0xFF191D26)
internal val TokenColorNeutral700 = Color(0xFF1E232E)
internal val TokenColorNeutral600 = Color(0xFF20242F)
internal val TokenColorNeutral400 = Color(0xFF6B7383)
internal val TokenColorNeutral300 = Color(0xFFA4ABB9)
internal val TokenColorNeutral100 = Color(0xFFF0F2F7)
internal val TokenColorNeutral050 = Color(0xFFF5F6FA)
internal val TokenColorNeutral100L = Color(0xFFEAEDF5)
internal val TokenColorNeutral200L = Color(0xFFCFD7E7)
internal val TokenColorNeutral500L = Color(0xFF828A9A)
internal val TokenColorNeutral600L = Color(0xFF4D5468)
internal val TokenColorNeutral900L = Color(0xFF0F1219)

// Beacon accent (primary) — accessibility-tuned: oklch hue 250
internal val TokenColorBeacon500   = Color(0xFF2A6FDB)  // oklch(58% 0.20 250) — primary action (≥4.5:1 on white)
internal val TokenColorBeacon600   = Color(0xFF1652B8)  // oklch(48% 0.20 250) — pressed / dim
internal val TokenColorBeaconSoftD = Color(0xFF131F38)  // oklch(22% 0.08 250) — accent bg on dark
internal val TokenColorBeaconSoftL = Color(0xFFDCE6F6)  // oklch(92% 0.05 250) — accent bg on light
internal val TokenColorBeaconTextD = Color(0xFF98BAF6)  // oklch(84% 0.13 250) — accent text on dark (~12:1)
internal val TokenColorBeaconTextL = Color(0xFF2A6FDB)  // same as Beacon500

// Status — success (teal) — unchanged
internal val TokenColorTealSoftD = Color(0xFF0E2B1C)
internal val TokenColorTealTextD = Color(0xFF4DC88A)
internal val TokenColorTealSoftL = Color(0xFFCCEEDD)
internal val TokenColorTealTextL = Color(0xFF115E30)

// Status — warning (amber) — unchanged
internal val TokenColorAmberSoftD = Color(0xFF251C08)
internal val TokenColorAmberTextD = Color(0xFFCDB84A)
internal val TokenColorAmberSoftL = Color(0xFFF5EFCA)
internal val TokenColorAmberTextL = Color(0xFF5C490A)

// Status — error / high priority (rose) — unchanged
internal val TokenColorRoseSoftD = Color(0xFF2A1410)
internal val TokenColorRoseTextD = Color(0xFFDF8065)
internal val TokenColorRoseSoftL = Color(0xFFFAEDE9)
internal val TokenColorRoseTextL = Color(0xFF8B3A20)

// ── Beacon accent (primary, accessibility-tuned) ─────────────
val beacon500   = Color(0xFF2A6FDB)  // oklch(58% 0.20 250) — primary action
val beacon600   = Color(0xFF1652B8)  // oklch(48% 0.20 250) — pressed / dim
val beaconSoftD = Color(0xFF131F38)  // oklch(22% 0.08 250) — accent bg on dark
val beaconSoftL = Color(0xFFDCE6F6)  // oklch(92% 0.05 250) — accent bg on light
val beaconTextD = Color(0xFF98BAF6)  // oklch(84% 0.13 250) — accent text on dark
val beaconTextL = Color(0xFF2A6FDB)  // same as beacon500 — accent text on light

// ── Phase pipeline colors — Post-Production retones to Beacon ─
val phaseDevelopment    = Color(0xFF3B7ED0)  // Sky
val phasePreProduction  = Color(0xFFB08A10)  // Amber
val phaseProduction     = Color(0xFF22A86A)  // Teal
val phasePostProduction = Color(0xFF2A6FDB)  // Beacon (was violet)
val phaseDistribution   = Color(0xFFD05838)  // Rose

val teal500     = Color(0xFF22A86A)  // oklch(52% 0.17 155)
val tealSoftD   = Color(0xFF0E2B1C)  // oklch(20% 0.07 155)
val tealSoftL   = Color(0xFFCCEEDD)  // oklch(91% 0.08 155)
val tealTextD   = Color(0xFF4DC88A)  // oklch(73% 0.12 155)
val tealTextL   = Color(0xFF115E30)  // oklch(33% 0.12 155)

// ── Amber (warning / medium priority) ───────────────────────
val amber500    = Color(0xFFB08A10)  // oklch(56% 0.18 75)
val amberSoftD  = Color(0xFF251C08)  // oklch(19% 0.07 80)
val amberSoftL  = Color(0xFFF5EFCA)  // oklch(93% 0.08 80)
val amberTextD  = Color(0xFFCDB84A)  // oklch(78% 0.12 80)
val amberTextL  = Color(0xFF5C490A)  // oklch(36% 0.12 80)

// ── Rose (high priority / error / warning) ───────────────────
val rose500     = Color(0xFFD05838)  // oklch(55% 0.17 20)
val roseSoftD   = Color(0xFF2A1410)  // oklch(20% 0.07 20)
val roseSoftL   = Color(0xFFFAEDE9)  // oklch(94% 0.07 20)
val roseTextD   = Color(0xFFDF8065)  // oklch(72% 0.14 20)
val roseTextL   = Color(0xFF8B3A20)  // oklch(38% 0.14 20)

val sky500      = Color(0xFF3B7ED0)  // oklch(55% 0.17 230)
val skySoftD    = Color(0xFF0E1E2D)  // oklch(20% 0.06 230)
val skySoftL    = Color(0xFFDDEAF7)  // oklch(93% 0.05 230)
val skyTextD    = Color(0xFF6FAEE8)  // oklch(75% 0.12 230)
val skyTextL    = Color(0xFF1B4A85)  // oklch(36% 0.13 230)
