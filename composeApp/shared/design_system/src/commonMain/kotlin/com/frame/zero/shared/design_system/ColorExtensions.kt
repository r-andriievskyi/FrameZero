package com.frame.zero.shared.design_system

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

fun Color.asColorFilter(): ColorFilter = ColorFilter.tint(this)
