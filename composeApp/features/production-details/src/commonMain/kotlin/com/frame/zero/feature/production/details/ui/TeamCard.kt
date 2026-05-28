package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.team_direct_reports
import framezero.composeapp.features.production_details.generated.resources.team_header
import framezero.composeapp.features.production_details.generated.resources.team_peers
import framezero.composeapp.features.production_details.generated.resources.team_reports_to
import framezero.composeapp.features.production_details.generated.resources.team_viewer_label
import org.jetbrains.compose.resources.stringResource
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ViewerCrew
import kotlin.time.Instant

private val AvatarSize = 44.dp
private val CountBadgePaddingHorizontal = 6.dp
private val CountBadgePaddingVertical = 2.dp

@Composable
internal fun TeamCard(
  viewerCrew: ViewerCrew,
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
    // Header: TEAM + viewer role badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.team_header),
        style = AppTheme.typographySystem.caption.copy(
          fontWeight = FontWeight.Bold
        ),
        color = AppTheme.colorSystem.textMuted
      )
      ViewerBadge(
        label = stringResource(Res.string.team_viewer_label, viewerCrew.viewer.role)
      )
    }

    // Reports To section
    viewerCrew.manager?.let { manager ->
      VerticalSpacer(AppTheme.spacingSystem.space16)
      SectionLabel(label = stringResource(Res.string.team_reports_to))
      VerticalSpacer(AppTheme.spacingSystem.space8)
      CrewRow(member = manager)
      VerticalSpacer(AppTheme.spacingSystem.space16)
      HorizontalDivider(
        thickness = AppTheme.borderSystem.hairline,
        color = AppTheme.colorSystem.border
      )
    }

    // Peers section
    if (viewerCrew.peers.isNotEmpty()) {
      VerticalSpacer(AppTheme.spacingSystem.space16)
      SectionHeader(
        label = stringResource(Res.string.team_peers),
        count = viewerCrew.peers.size
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      viewerCrew.peers.forEachIndexed { index, member ->
        CrewRow(member = member)
        if (index < viewerCrew.peers.lastIndex) {
          VerticalSpacer(AppTheme.spacingSystem.space8)
        }
      }
      VerticalSpacer(AppTheme.spacingSystem.space16)
      HorizontalDivider(
        thickness = AppTheme.borderSystem.hairline,
        color = AppTheme.colorSystem.border
      )
    }

    // Direct Reports section
    if (viewerCrew.reports.isNotEmpty()) {
      VerticalSpacer(AppTheme.spacingSystem.space16)
      SectionHeader(
        label = stringResource(Res.string.team_direct_reports),
        count = viewerCrew.reports.size
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      viewerCrew.reports.forEachIndexed { index, member ->
        CrewRow(member = member)
        if (index < viewerCrew.reports.lastIndex) {
          VerticalSpacer(AppTheme.spacingSystem.space8)
        }
      }
    }
  }
}

@Composable
private fun ViewerBadge(
  label: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = label,
    style = AppTheme.typographySystem.bodySmall.copy(
      fontWeight = FontWeight.Medium
    ),
    color = AppTheme.colorSystem.accentText,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(AppTheme.colorSystem.accentSurface)
      .padding(
        horizontal = AppTheme.spacingSystem.space8,
        vertical = AppTheme.spacingSystem.space4
      )
  )
}

@Composable
private fun SectionLabel(
  label: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = label,
    style = AppTheme.typographySystem.caption.copy(
      fontWeight = FontWeight.Bold
    ),
    color = AppTheme.colorSystem.textMuted,
    modifier = modifier
  )
}

@Composable
private fun SectionHeader(
  label: String,
  count: Int,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(
      AppTheme.spacingSystem.space8
    )
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.caption.copy(
        fontWeight = FontWeight.Bold
      ),
      color = AppTheme.colorSystem.textMuted
    )
    Text(
      text = "$count",
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted,
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .background(AppTheme.colorSystem.border)
        .padding(
          horizontal = CountBadgePaddingHorizontal,
          vertical = CountBadgePaddingVertical
        )
    )
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
        .clip(CircleShape)
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

// ── Previews ────────────────────────────────────────────────────────────

private val PreviewInstant = Instant.fromEpochMilliseconds(0L)

@Preview
@Composable
private fun TeamCardPreview() {
  AppTheme(darkTheme = true) {
    TeamCard(
      viewerCrew = ViewerCrew(
        viewer = ProductionMember(
          id = "m2",
          userId = "u-me",
          name = "Tom Ellison",
          role = "Producer",
          initials = "TE",
          avatarColorHex = "#2196F3",
          addedAt = PreviewInstant,
          reportsToMemberId = "m1"
        ),
        manager = ProductionMember(
          id = "m1",
          userId = null,
          name = "Maya Rivera",
          role = "Director",
          initials = "MR",
          avatarColorHex = "#E91E63",
          addedAt = PreviewInstant,
          reportsToMemberId = null
        ),
        peers = listOf(
          ProductionMember(
            id = "m3",
            userId = null,
            name = "Sara Lin",
            role = "Cinematographer",
            initials = "SL",
            avatarColorHex = "#9C27B0",
            addedAt = PreviewInstant,
            reportsToMemberId = "m1"
          )
        ),
        reports = listOf(
          ProductionMember(
            id = "m4",
            userId = null,
            name = "Jake Morse",
            role = "1st AD",
            initials = "JM",
            avatarColorHex = "#009688",
            addedAt = PreviewInstant,
            reportsToMemberId = "m2"
          ),
          ProductionMember(
            id = "m5",
            userId = null,
            name = "Sara Lin",
            role = "Cinematographer",
            initials = "SL",
            avatarColorHex = "#9C27B0",
            addedAt = PreviewInstant,
            reportsToMemberId = "m2"
          )
        )
      )
    )
  }
}
