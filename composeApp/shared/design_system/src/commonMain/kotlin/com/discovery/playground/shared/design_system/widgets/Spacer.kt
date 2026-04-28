package com.discovery.playground.shared.design_system.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VerticalSpacer(
  spaceBy: Dp,
  modifier: Modifier = Modifier,
) = Spacer(modifier = modifier.height(spaceBy))

@Composable
fun HorizontalSpacer(
  spaceBy: Dp,
  modifier: Modifier = Modifier,
) = Spacer(modifier = modifier.width(spaceBy))
