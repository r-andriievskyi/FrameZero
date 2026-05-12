package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionPipelinePhase
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsIntent
import com.frame.zero.feature.production.details.ProductionDetailsState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

// ── Dimension constants ─────────────────────────────────────────────────
private val ActionButtonSize = 40.dp
private val ProgressBarHeight = 6.dp
private val GradientBarHeight = 4.dp
private val AvatarSize = 44.dp
private val PhaseBarSegmentHeight = 6.dp
private val OverflowMenuWidth = 200.dp
private val CardBorderWidth = 1.dp
private val PhaseDotSize = 16.dp

private const val OverlayAlpha = 0.6f

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
        subtitle = state.detail?.let {
          "${it.genre.displayLabel()} · ${it.phase.displayLabel()}"
        }.orEmpty(),
        canDelete = state.detail != null && !state.isDeleting,
        onBack = onBack,
        onDeleteClick = { onIntent(ProductionDetailsIntent.DeleteRequested) }
      )

      val loadError = state.error
      when {
        state.isLoading && state.detail == null -> CenteredProgress()
        loadError != null && state.detail == null -> CenteredMessage(loadError)
        state.detail != null -> DetailBody(detail = state.detail!!)
        else -> Box(modifier = Modifier.fillMaxSize())
      }
    }

    if (state.isDeleting) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            AppTheme.colorSystem.background.copy(alpha = OverlayAlpha)
          ),
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

// ── Top Bar ─────────────────────────────────────────────────────────────

@Composable
private fun DetailsTopBar(
  title: String,
  subtitle: String,
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
    BackButton(onClick = onBack)
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (subtitle.isNotBlank()) {
        Text(
          text = subtitle,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
    }
    if (canDelete) {
      OverflowMenuButton(onDeleteClick = onDeleteClick)
    }
  }
}

@Composable
private fun BackButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(ActionButtonSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(AppTheme.colorSystem.cardBackground)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "‹",
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Composable
private fun OverflowMenuButton(
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .size(ActionButtonSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .border(
          width = CardBorderWidth,
          color = AppTheme.colorSystem.cardBorder,
          shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
        )
        .clickable { expanded = true },
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "···",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(OverflowMenuWidth)
        .background(AppTheme.colorSystem.surfaceElevated)
    ) {
      DropdownMenuItem(
        text = {
          Text(
            text = "Delete production",
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.errorText
          )
        },
        onClick = {
          expanded = false
          onDeleteClick()
        }
      )
    }
  }
}

// ── Detail Body ─────────────────────────────────────────────────────────

@Composable
private fun DetailBody(
  detail: ProductionDetail,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    GradientAccentBar(phase = detail.phase)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    LoglineCard(logline = detail.logline, detail = detail)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    PipelineCard(
      pipeline = detail.pipeline,
      currentPhase = detail.phase
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    DateCards(startDate = detail.startDate, wrapDate = detail.wrapDate)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    KeyCrewCard(crew = detail.keyCrew)
    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

// ── Gradient Accent Bar ─────────────────────────────────────────────────

@Composable
private fun GradientAccentBar(
  phase: ProductionPhase,
  modifier: Modifier = Modifier
) {
  val phaseColor = phaseAccentColor(phase)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(GradientBarHeight)
      .background(
        Brush.horizontalGradient(
          colors = listOf(phaseColor, AppTheme.colorSystem.accent)
        )
      )
  )
}

// ── Logline + Progress + Stats Card ─────────────────────────────────────

@Composable
private fun LoglineCard(
  logline: String?,
  detail: ProductionDetail,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    if (!logline.isNullOrBlank()) {
      Text(
        text = "\"$logline\"",
        style = AppTheme.typographySystem.bodyMedium.copy(
          fontStyle = FontStyle.Italic
        ),
        color = AppTheme.colorSystem.textSecondary
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
      HorizontalDivider(
        thickness = CardBorderWidth,
        color = AppTheme.colorSystem.border
      )
      VerticalSpacer(AppTheme.spacingSystem.space16)
    }

    // Overall progress
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "OVERALL PROGRESS",
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "${detail.progressPercent}%",
        style = AppTheme.typographySystem.labelMedium,
        color = phaseAccentColor(detail.phase)
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space8)
    GradientProgressBar(
      progress = detail.progressPercent / 100f,
      phase = detail.phase
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Stats row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        AppTheme.spacingSystem.space8
      )
    ) {
      StatItem(
        value = "${detail.membersCount}",
        label = "Members",
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = "${detail.daysLeft}d",
        label = "Days left",
        modifier = Modifier.weight(1f)
      )
      StatItem(
        value = formatBudget(detail.budgetCents),
        label = "Budget",
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun GradientProgressBar(
  progress: Float,
  phase: ProductionPhase,
  modifier: Modifier = Modifier
) {
  val phaseColor = phaseAccentColor(phase)
  val trackColor = AppTheme.colorSystem.border
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(ProgressBarHeight)
      .clip(shape)
      .background(trackColor)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
        .height(ProgressBarHeight)
        .clip(shape)
        .background(
          Brush.horizontalGradient(
            colors = listOf(phaseColor, AppTheme.colorSystem.accent)
          )
        )
    )
  }
}

@Composable
private fun StatItem(
  value: String,
  label: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .border(
        width = CardBorderWidth,
        color = AppTheme.colorSystem.cardBorder,
        shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space16
      ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = value,
      style = AppTheme.typographySystem.titleMedium.copy(
        fontWeight = FontWeight.Bold
      ),
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = label,
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

// ── Pipeline Card ───────────────────────────────────────────────────────

@Composable
private fun PipelineCard(
  pipeline: List<ProductionPipelinePhase>,
  currentPhase: ProductionPhase,
  modifier: Modifier = Modifier
) {
  val currentIndex = pipeline.indexOfFirst { it.isCurrent }
  val totalPhases = pipeline.size
  val previousPhase = pipeline.getOrNull(currentIndex - 1)
  val nextPhase = pipeline.getOrNull(currentIndex + 1)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "PIPELINE",
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "Phase ${currentIndex + 1} of $totalPhases",
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Current phase with dot
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(PhaseDotSize)
          .clip(CircleShape)
          .background(phaseAccentColor(currentPhase))
      )
      HorizontalSpacer(AppTheme.spacingSystem.space8)
      Column {
        Text(
          text = currentPhase.displayLabel(),
          style = AppTheme.typographySystem.titleMedium,
          color = AppTheme.colorSystem.textPrimary
        )
        Row {
          if (previousPhase != null) {
            Text(
              text = "← ${previousPhase.label}",
              style = AppTheme.typographySystem.bodySmall,
              color = AppTheme.colorSystem.textMuted
            )
          }
          if (previousPhase != null && nextPhase != null) {
            Text(
              text = " · ",
              style = AppTheme.typographySystem.bodySmall,
              color = AppTheme.colorSystem.textMuted
            )
          }
          if (nextPhase != null) {
            Text(
              text = "${nextPhase.label} →",
              style = AppTheme.typographySystem.bodySmall,
              color = AppTheme.colorSystem.textMuted
            )
          }
        }
      }
    }
    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Phase bar segments
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        AppTheme.spacingSystem.space4
      )
    ) {
      pipeline.forEach { phase ->
        val segmentColor = when {
          phase.isCompleted || phase.isCurrent ->
            phaseAccentColor(phase.phase)
          else -> AppTheme.colorSystem.border
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .height(PhaseBarSegmentHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(segmentColor)
        )
      }
    }
  }
}

// ── Date Cards ──────────────────────────────────────────────────────────

@Composable
private fun DateCards(
  startDate: LocalDate,
  wrapDate: LocalDate,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16),
    horizontalArrangement = Arrangement.spacedBy(
      AppTheme.spacingSystem.space8
    )
  ) {
    DateCard(
      label = "START DATE",
      date = startDate.formatDisplay(),
      modifier = Modifier.weight(1f)
    )
    DateCard(
      label = "WRAP DATE",
      date = wrapDate.formatDisplay(),
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun DateCard(
  label: String,
  date: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted
    )
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = date,
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

// ── Key Crew Card ───────────────────────────────────────────────────────

@Composable
private fun KeyCrewCard(
  crew: List<ProductionMember>,
  modifier: Modifier = Modifier
) {
  if (crew.isEmpty()) return
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = "KEY CREW",
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    crew.forEachIndexed { index, member ->
      CrewRow(member = member)
      if (index < crew.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space16)
      }
    }
  }
}

@Composable
private fun CrewRow(
  member: ProductionMember,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val avatarColor = member.avatarColorHex
      ?.let { parseHexColor(it) }
      ?: AppTheme.colorSystem.accentDim
    Box(
      modifier = Modifier
        .size(AvatarSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(avatarColor),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = member.initials,
        style = AppTheme.typographySystem.labelMedium,
        color = AppTheme.colorSystem.textOnAccent
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space16)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = member.name,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      Text(
        text = member.role,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Text(
      text = "›",
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

// ── Shared helpers ──────────────────────────────────────────────────────

@Composable
private fun CenteredProgress(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(color = AppTheme.colorSystem.accent)
  }
}

@Composable
private fun CenteredMessage(
  message: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
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

// ── Dialogs ─────────────────────────────────────────────────────────────

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

// ── Utility functions ───────────────────────────────────────────────────

@Composable
private fun phaseAccentColor(phase: ProductionPhase): Color =
  when (phase) {
    ProductionPhase.IDEA -> AppTheme.colorSystem.textMuted
    ProductionPhase.DEVELOPMENT -> AppTheme.colorSystem.developmentText
    ProductionPhase.FINANCING -> AppTheme.colorSystem.warningText
    ProductionPhase.PRE_PRODUCTION ->
      AppTheme.colorSystem.preProductionText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.productionText
    ProductionPhase.POST_PRODUCTION ->
      AppTheme.colorSystem.postProductionText
    ProductionPhase.MARKETING -> AppTheme.colorSystem.accentText
    ProductionPhase.DISTRIBUTION ->
      AppTheme.colorSystem.distributionText
    ProductionPhase.RELEASE -> AppTheme.colorSystem.successText
    ProductionPhase.ARCHIVED -> AppTheme.colorSystem.textMuted
  }

private fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

private fun Genre.displayLabel(): String =
  name.replace('_', ' ').lowercase()
    .replaceFirstChar { it.uppercase() }

private fun LocalDate.formatDisplay(): String {
  val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  )
  return "${monthNames[month.ordinal]} $day, $year"
}

private fun formatBudget(cents: Long?): String {
  if (cents == null) return "—"
  val dollars = cents / 100
  return "$${
    dollars.toString().reversed().chunked(3)
      .joinToString(",").reversed()
  }"
}

@Suppress("MagicNumber")
private fun parseHexColor(hex: String): Color? {
  val cleaned = hex.removePrefix("#")
  return runCatching {
    Color(("FF$cleaned").toLong(16))
  }.getOrNull()
}

// ── Previews ────────────────────────────────────────────────────────────

private val PreviewInstant = Instant.fromEpochMilliseconds(0L)

@Preview
@Composable
private fun ProductionDetailsLoadedPreview() {
  AppTheme(darkTheme = true) {
    ProductionDetailsScreen(
      state = ProductionDetailsState(
        detail = ProductionDetail(
          id = "1",
          title = "Echoes of Silence",
          genre = Genre.DRAMA,
          logline = "A deaf composer rediscovers sound through" +
            " the chaos of war.",
          phase = ProductionPhase.PRODUCTION,
          progressPercent = 68,
          daysLeft = 24,
          startDate = LocalDate(2026, 2, 10),
          wrapDate = LocalDate(2026, 8, 30),
          budgetCents = 240_000_000L,
          membersCount = 12,
          keyCrew = listOf(
            ProductionMember(
              id = "m1", userId = null,
              name = "Maya Rivera", role = "Director",
              initials = "MR", avatarColorHex = "#E91E63",
              addedAt = PreviewInstant
            ),
            ProductionMember(
              id = "m2", userId = null,
              name = "Tom Ellison", role = "Producer",
              initials = "TE", avatarColorHex = "#2196F3",
              addedAt = PreviewInstant
            ),
            ProductionMember(
              id = "m3", userId = null,
              name = "Sara Lin", role = "DP",
              initials = "SL", avatarColorHex = "#9C27B0",
              addedAt = PreviewInstant
            ),
            ProductionMember(
              id = "m4", userId = null,
              name = "Jake Morse", role = "1st AD",
              initials = "JM", avatarColorHex = "#009688",
              addedAt = PreviewInstant
            )
          ),
          pipeline = ProductionPhase.entries.map { p ->
            ProductionPipelinePhase(
              phase = p,
              label = p.displayLabel(),
              isCompleted =
                p.ordinal < ProductionPhase.PRODUCTION.ordinal,
              isCurrent = p == ProductionPhase.PRODUCTION
            )
          },
          createdAt = PreviewInstant,
          updatedAt = PreviewInstant
        )
      ),
      onBack = {},
      onIntent = {}
    )
  }
}

@Preview
@Composable
private fun ProductionDetailsLoadingPreview() {
  AppTheme(darkTheme = true) {
    ProductionDetailsScreen(
      state = ProductionDetailsState(isLoading = true),
      onBack = {},
      onIntent = {}
    )
  }
}
