package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsIntent
import com.frame.zero.feature.production.details.ProductionDetailsState

private val ActionButtonSize = 40.dp

@Composable
fun ProductionDetailsContent(component: ProductionDetailsComponent) {
  val state by component.state.collectAsState()
  ProductionDetailsScreen(
    state = state,
    onBack = component.onBack,
    onIntent = component::onIntent
  )
}

@Composable
internal fun ProductionDetailsScreen(
  state: ProductionDetailsState,
  onBack: () -> Unit,
  onIntent: (ProductionDetailsIntent) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      DetailsTopBar(
        title = state.detail?.title.orEmpty(),
        canDelete = state.detail != null && !state.isDeleting,
        onBack = onBack,
        onDeleteClick = { onIntent(ProductionDetailsIntent.DeleteRequested) }
      )

      val loadError = state.error
      when {
        state.isLoading && state.detail == null -> CenteredProgress()
        loadError != null && state.detail == null -> CenteredMessage(loadError)
        else -> Box(modifier = Modifier.fillMaxSize())
      }
    }

    if (state.isDeleting) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(AppTheme.colorSystem.background.copy(alpha = OverlayAlpha)),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = AppTheme.colorSystem.accent)
      }
    }
  }

  if (state.isDeleteDialogVisible) {
    DeleteConfirmDialog(
      title = state.detail?.title.orEmpty(),
      onConfirm = { onIntent(ProductionDetailsIntent.DeleteConfirmed) },
      onDismiss = { onIntent(ProductionDetailsIntent.DeleteDismissed) }
    )
  }

  state.deleteError?.let { message ->
    DeleteErrorDialog(
      message = message,
      onDismiss = { onIntent(ProductionDetailsIntent.DeleteErrorDismissed) }
    )
  }
}

private const val OverlayAlpha = 0.6f

@Composable
private fun DetailsTopBar(
  title: String,
  canDelete: Boolean,
  onBack: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onBack, label = "<")
    Text(
      text = title,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = AppTheme.spacingSystem.space8)
    )
    if (canDelete) {
      Box(
        modifier = Modifier
          .size(ActionButtonSize)
          .clip(RoundedCornerShape(AppTheme.spacingSystem.space8))
          .background(AppTheme.colorSystem.errorSurface)
          .clickable(onClick = onDeleteClick),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Delete",
          style = AppTheme.typographySystem.labelSmall,
          color = AppTheme.colorSystem.errorText
        )
      }
    }
  }
}

@Composable
private fun IconButton(
  onClick: () -> Unit,
  label: String
) {
  Box(
    modifier = Modifier
      .size(ActionButtonSize)
      .clip(RoundedCornerShape(AppTheme.spacingSystem.space8))
      .background(AppTheme.colorSystem.cardBackground)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Composable
private fun CenteredProgress() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(color = AppTheme.colorSystem.accent)
  }
}

@Composable
private fun CenteredMessage(message: String) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(AppTheme.spacingSystem.space16),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = message,
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )
  }
}

@Composable
private fun DeleteConfirmDialog(
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
          "\"$title\" will be removed for all members. This cannot be undone."
        } else {
          "This production will be removed for all members. This cannot be undone."
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
private fun DeleteErrorDialog(
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

@Preview
@Composable
private fun ProductionDetailsPreview() {
  AppTheme(darkTheme = true) {
    ProductionDetailsScreen(
      state = ProductionDetailsState(),
      onBack = {},
      onIntent = {}
    )
  }
}
