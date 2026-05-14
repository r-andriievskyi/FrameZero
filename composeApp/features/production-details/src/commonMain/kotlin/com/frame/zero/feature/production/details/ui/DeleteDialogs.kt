package com.frame.zero.feature.production.details.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.discovery.playground.shared.design_system.AppTheme
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.cancel_action
import framezero.composeapp.features.production_details.generated.resources.couldnt_delete
import framezero.composeapp.features.production_details.generated.resources.delete_action
import framezero.composeapp.features.production_details.generated.resources.delete_production_body_named
import framezero.composeapp.features.production_details.generated.resources.delete_production_body_unnamed
import framezero.composeapp.features.production_details.generated.resources.delete_production_title
import framezero.composeapp.features.production_details.generated.resources.ok_action
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeleteConfirmDialog(
  title: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(Res.string.delete_production_title),
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    },
    text = {
      Text(
        text = if (title.isNotBlank()) {
          stringResource(Res.string.delete_production_body_named, title)
        } else {
          stringResource(Res.string.delete_production_body_unnamed)
        },
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textSecondary
      )
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = stringResource(Res.string.delete_action),
          style = AppTheme.typographySystem.labelLarge,
          color = AppTheme.colorSystem.errorText
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(Res.string.cancel_action),
          style = AppTheme.typographySystem.labelLarge,
          color = AppTheme.colorSystem.textPrimary
        )
      }
    },
    containerColor = AppTheme.colorSystem.surfaceElevated
  )
}

@Composable
internal fun DeleteErrorDialog(
  message: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(Res.string.couldnt_delete),
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    },
    text = {
      Text(
        text = message,
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textSecondary
      )
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(Res.string.ok_action),
          style = AppTheme.typographySystem.labelLarge,
          color = AppTheme.colorSystem.accent
        )
      }
    },
    containerColor = AppTheme.colorSystem.surfaceElevated
  )
}

