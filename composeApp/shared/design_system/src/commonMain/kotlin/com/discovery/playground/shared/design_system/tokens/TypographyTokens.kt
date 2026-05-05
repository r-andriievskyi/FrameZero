package com.discovery.playground.shared.design_system.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DMSans = FontFamily.SansSerif
private val DMMono = FontFamily.Monospace

// ── Display ──────────────────────────────────────────────
val DisplayLarge =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    letterSpacing = (-0.5).sp
  )

val DisplayMedium =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    letterSpacing = (-0.5).sp
  )

// ── Titles ───────────────────────────────────────────────
val TitleLarge =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    letterSpacing = (-0.3).sp
  )

val TitleMedium =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp
  )

val TitleSmall =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp
  )

// ── Body ─────────────────────────────────────────────────
val BodyLarge =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
  )

val BodyMedium =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
  )

val BodySmall =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
  )

// ── Labels ───────────────────────────────────────────────
val LabelLarge =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp
  )

val LabelMedium =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp
  )

val LabelSmall =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    letterSpacing = 0.7.sp
  )

// ── Caption ──────────────────────────────────────────────
val Caption =
  TextStyle(
    fontFamily = DMSans,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp
  )

// ── Mono ─────────────────────────────────────────────────
val MonoMedium =
  TextStyle(
    fontFamily = DMMono,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp
  )

val MonoSmall =
  TextStyle(
    fontFamily = DMMono,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp
  )
