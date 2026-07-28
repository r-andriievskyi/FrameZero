package com.frame.zero.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String =
  when (this) {
    is UiText.Resource -> stringResource(res, *args.toTypedArray())
  }
