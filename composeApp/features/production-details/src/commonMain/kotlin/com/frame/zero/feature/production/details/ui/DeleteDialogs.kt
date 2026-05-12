package com.frame.zero.feature.production.details.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.discovery.playground.shared.design_system.AppTheme

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
        text = "Delete production?",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    },
    text = {
      Text(
        text = if (title.isNotBlank()) {
          "\"$title\" will be removed for all members." +
            " This cannot be undone."
        } else {
          "This production will be removed for all members." +
            " This cannot be undone."
        },
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textSecondary
      )
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = "Delete",
          style = AppTheme.typographySystem.labelLarge,
          color = AppTheme.colorSystem.errorText
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = "Cancel",
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
        text = "Couldn't delete",
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
          text = "OK",
          style = AppTheme.typographySystem.labelLarge,
          color = AppTheme.colorSystem.accent
        )
      }
    },
    containerColor = AppTheme.colorSystem.surfaceElevated
  )
}

