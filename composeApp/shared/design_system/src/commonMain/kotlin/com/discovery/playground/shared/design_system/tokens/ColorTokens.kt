package com.discovery.playground.shared.design_system.tokens

import androidx.compose.ui.graphics.Color

internal val TokenColorWhite = Color(0xFFFFFFFF)

// Neutral scale (violet-tinted)
internal val TokenColorNeutral900 = Color(0xFF0D0B12)
internal val TokenColorNeutral850 = Color(0xFF110E16)
internal val TokenColorNeutral800 = Color(0xFF18151F)
internal val TokenColorNeutral780 = Color(0xFF161320)
internal val TokenColorNeutral750 = Color(0xFF1C1924)
internal val TokenColorNeutral700 = Color(0xFF232030)
internal val TokenColorNeutral600 = Color(0xFF252131)
internal val TokenColorNeutral400 = Color(0xFF70647F)
internal val TokenColorNeutral300 = Color(0xFFA89CB8)
internal val TokenColorNeutral100 = Color(0xFFF2F0F7)
internal val TokenColorNeutral050 = Color(0xFFF7F5FC)
internal val TokenColorNeutral100L = Color(0xFFEDE9F6)
internal val TokenColorNeutral200L = Color(0xFFD5CEEA)
internal val TokenColorNeutral500L = Color(0xFF8B81A0)
internal val TokenColorNeutral600L = Color(0xFF574D6E)
internal val TokenColorNeutral900L = Color(0xFF130F1C)

// Violet accent
internal val TokenColorViolet500 = Color(0xFF8055E8)
internal val TokenColorViolet600 = Color(0xFF6A40D0)
internal val TokenColorVioletSoftD = Color(0xFF2A1B4E)
internal val TokenColorVioletSoftL = Color(0xFFEDE8FA)
internal val TokenColorVioletTextD = Color(0xFFB49AEF)

// Status — success (teal)
internal val TokenColorTealSoftD = Color(0xFF0E2B1C)
internal val TokenColorTealTextD = Color(0xFF4DC88A)
internal val TokenColorTealSoftL = Color(0xFFCCEEDD)
internal val TokenColorTealTextL = Color(0xFF115E30)

// Status — warning (amber)
internal val TokenColorAmberSoftD = Color(0xFF251C08)
internal val TokenColorAmberTextD = Color(0xFFCDB84A)
internal val TokenColorAmberSoftL = Color(0xFFF5EFCA)
internal val TokenColorAmberTextL = Color(0xFF5C490A)

// Status — error / high priority (rose)
internal val TokenColorRoseSoftD = Color(0xFF2A1410)
internal val TokenColorRoseTextD = Color(0xFFDF8065)
internal val TokenColorRoseSoftL = Color(0xFFFAEDE9)
internal val TokenColorRoseTextL = Color(0xFF8B3A20)

// ── Violet accent (primary) ──────────────────────────────────
val violet500   = Color(0xFF8055E8)  // oklch(58% 0.19 290) — primary action
val violet600   = Color(0xFF6A40D0)  // oklch(48% 0.19 290) — pressed / dim state
val violetSoftD = Color(0xFF2A1B4E)  // oklch(25% 0.08 290) — accent bg on dark
val violetSoftL = Color(0xFFEDE8FA)  // oklch(94% 0.06 290) — accent bg on light
val violetTextD = Color(0xFFB49AEF)  // oklch(78% 0.14 290) — accent text on dark bg
val violetTextL = Color(0xFF8055E8)  // same as violet500  — accent text on light bg

// ── Teal (secondary accent) ──────────────────────────────────
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

// ── Sky (info / Development phase) ───────────────────────────
val sky500      = Color(0xFF3B7ED0)  // oklch(55% 0.17 230)
val skySoftD    = Color(0xFF0E1E2D)  // oklch(20% 0.06 230)
val skySoftL    = Color(0xFFDDEAF7)  // oklch(93% 0.05 230)
val skyTextD    = Color(0xFF6FAEE8)  // oklch(75% 0.12 230)
val skyTextL    = Color(0xFF1B4A85)  // oklch(36% 0.13 230)

// ── Phase pipeline colors (raw, not theme-aware) ─────────────
val phaseDevelopment    = Color(0xFF3B7ED0)  // oklch(55% 0.17 230) — Sky
val phasePreProduction  = Color(0xFFB08A10)  // oklch(56% 0.18 75)  — Amber
val phaseProduction     = Color(0xFF22A86A)  // oklch(52% 0.17 155) — Teal
val phasePostProduction = Color(0xFF7844DE)  // oklch(50% 0.18 290) — Violet
val phaseDistribution   = Color(0xFFD05838)  // oklch(55% 0.17 20)  — Rose
