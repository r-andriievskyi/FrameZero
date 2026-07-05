package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.features.chat.generated.resources.Res
import framezero.composeapp.features.chat.generated.resources.chat_new_messages
import org.jetbrains.compose.resources.stringResource

private val DividerLineThickness = 1.dp

/**
 * The "New messages" marker: an accent hairline with a centered pill label. The list places it
 * above the first unread message, so under `reverseLayout` it separates the read block (above)
 * from the unread block (below).
 */
@Composable
internal fun NewMessagesDivider(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = AppTheme.spacingSystem.space8),
    verticalAlignment = Alignment.CenterVertically
  ) {
    DividerLine(modifier = Modifier.weight(1f))
    PillLabel(
      text = stringResource(Res.string.chat_new_messages).uppercase(),
      backgroundColor = AppTheme.colorSystem.accentDim,
      contentColor = AppTheme.colorSystem.accent,
      modifier = Modifier.padding(horizontal = AppTheme.spacingSystem.space12)
    )
    DividerLine(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .height(DividerLineThickness)
      .background(AppTheme.colorSystem.accent)
  )
}

@LightDarkPreview
@Composable
private fun NewMessagesDividerPreview() {
  AppTheme {
    Box(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      NewMessagesDivider()
    }
  }
}
