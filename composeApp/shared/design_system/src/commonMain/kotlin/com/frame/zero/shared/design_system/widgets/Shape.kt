package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp

@Composable
fun rememberRoundedCornerShape(radius: Dp): RoundedCornerShape = remember(radius) { RoundedCornerShape(radius) }
