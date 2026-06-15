package com.frame.zero.shared.design_system.style

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import com.frame.zero.shared.design_system.BorderSystem
import com.frame.zero.shared.design_system.ColorSystem
import com.frame.zero.shared.design_system.LocalAppTheme
import com.frame.zero.shared.design_system.RadiusSystem
import com.frame.zero.shared.design_system.SpacingSystem
import com.frame.zero.shared.design_system.TypographySystem

@OptIn(ExperimentalFoundationStyleApi::class)
internal val StyleScope.colorSystem: ColorSystem
  get() = LocalAppTheme.currentValue.colorSystem

@OptIn(ExperimentalFoundationStyleApi::class)
internal val StyleScope.typographySystem: TypographySystem
  get() = LocalAppTheme.currentValue.typographySystem

@OptIn(ExperimentalFoundationStyleApi::class)
internal val StyleScope.spacingSystem: SpacingSystem
  get() = LocalAppTheme.currentValue.spacingSystem

@OptIn(ExperimentalFoundationStyleApi::class)
internal val StyleScope.radiusSystem: RadiusSystem
  get() = LocalAppTheme.currentValue.radiusSystem

@OptIn(ExperimentalFoundationStyleApi::class)
internal val StyleScope.borderSystem: BorderSystem
  get() = LocalAppTheme.currentValue.borderSystem
