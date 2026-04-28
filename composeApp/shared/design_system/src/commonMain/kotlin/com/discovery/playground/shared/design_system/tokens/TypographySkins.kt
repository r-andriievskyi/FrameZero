package com.discovery.playground.shared.design_system.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val DiscoveryLineHeightStyle =
  LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
  )

private fun lineHeight(
  fontSize: TextUnit,
  ratio: Float,
): TextUnit = (fontSize.value * ratio).sp

internal fun fontTitleSection(fontFamily: FontFamily) =
  TextStyle(
    fontFamily = fontFamily,
    fontWeight = TokenFontWeightBold,
    fontSize = TokenFontSize20,
    letterSpacing = TokenLetterSpacing050,
    lineHeight = lineHeight(TokenFontSize20, TokenLineHeightMultiplier13),
    lineHeightStyle = DiscoveryLineHeightStyle,
  )

internal fun fontBodyStandard(fontFamily: FontFamily) =
  TextStyle(
    fontFamily = fontFamily,
    fontWeight = TokenFontWeightRegular,
    fontSize = TokenFontSize16,
    letterSpacing = TokenLetterSpacing100,
    lineHeight = lineHeight(TokenFontSize16, TokenLineHeightMultiplier15),
    lineHeightStyle = DiscoveryLineHeightStyle,
  )

internal fun fontBodySmall(fontFamily: FontFamily) =
  TextStyle(
    fontFamily = fontFamily,
    fontWeight = TokenFontWeightRegular,
    fontSize = TokenFontSize14,
    letterSpacing = TokenLetterSpacing100,
    lineHeight = lineHeight(TokenFontSize14, TokenLineHeightMultiplier15),
    lineHeightStyle = DiscoveryLineHeightStyle,
  )

internal fun fontLabel(fontFamily: FontFamily) =
  TextStyle(
    fontFamily = fontFamily,
    fontWeight = TokenFontWeightMedium,
    fontSize = TokenFontSize14,
    letterSpacing = TokenLetterSpacing050,
    lineHeight = lineHeight(TokenFontSize14, TokenLineHeightMultiplier13),
    lineHeightStyle = DiscoveryLineHeightStyle,
  )

internal fun fontButton(fontFamily: FontFamily) =
  TextStyle(
    fontFamily = fontFamily,
    fontWeight = TokenFontWeightMedium,
    fontSize = TokenFontSize16,
    letterSpacing = TokenLetterSpacing050,
    lineHeight = lineHeight(TokenFontSize16, TokenLineHeightMultiplier13),
    lineHeightStyle = DiscoveryLineHeightStyle,
  )
