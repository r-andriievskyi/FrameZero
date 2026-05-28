package com.frame.zero.feature.account.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.sign_out
import org.jetbrains.compose.resources.stringResource

private val SignOutHeight = 52.dp

@Composable
internal fun SignOutButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(SignOutHeight)
      .clip(shape)
      .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.errorText, shape)
      .clickableWithRipple(
        color = AppTheme.colorSystem.errorSurface,
        bounded = true,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(Res.string.sign_out),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.errorText,
      fontWeight = FontWeight.Medium
    )
  }
}

@Preview
@Composable
private fun SignOutButtonPreview() {
  AppTheme(darkTheme = true) {
    SignOutButton(onClick = {})
  }
}
