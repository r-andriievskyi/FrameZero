package com.discovery.playground.shared.design_system.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.discovery.playground.shared.design_system.generated.resources.Res
import com.discovery.playground.shared.design_system.generated.resources.graphik_bold
import com.discovery.playground.shared.design_system.generated.resources.graphik_medium
import com.discovery.playground.shared.design_system.generated.resources.graphik_regular
import com.discovery.playground.shared.design_system.generated.resources.graphik_semibold
import org.jetbrains.compose.resources.Font

@Composable
internal fun rememberFontFamilyPrimary(): FontFamily {
  val regular = Font(Res.font.graphik_regular, weight = FontWeight.Normal)
  val medium = Font(Res.font.graphik_medium, weight = FontWeight.Medium)
  val semiBold = Font(Res.font.graphik_semibold, weight = FontWeight.SemiBold)
  val bold = Font(Res.font.graphik_bold, weight = FontWeight.Bold)
  return remember(regular, medium, semiBold, bold) {
    FontFamily(regular, medium, semiBold, bold)
  }
}
