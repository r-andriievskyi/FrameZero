package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.production.details.ProductionMemberUi
import com.frame.zero.feature.production.details.ViewerCrewUi
import kotlinx.collections.immutable.persistentListOf
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_right
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.production_details.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.Res as DesignSystemRes
import framezero.composeapp.features.production_details.generated.resources.team_direct_reports
import framezero.composeapp.features.production_details.generated.resources.team_header
import framezero.composeapp.features.production_details.generated.resources.team_peers
import framezero.composeapp.features.production_details.generated.resources.team_reports_to
import framezero.composeapp.features.production_details.generated.resources.team_viewer_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val AvatarSize = 44.dp
private val CountBadgePaddingHorizontal = 6.dp
private val CountBadgePaddingVertical = 2.dp

@Composable
internal fun TeamCard(
  viewerCrew: ViewerCrewUi,
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
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        modifier = Modifier.semantics { heading() },
        text = stringResource(Res.string.team_header),
        style = AppTheme.typographySystem.caption.copy(
          fontWeight = FontWeight.Bold
        ),
        color = AppTheme.colorSystem.textMuted
      )
      ViewerBadge(
        label = stringResource(Res.string.team_viewer_label, viewerCrew.viewerRole)
      )
    }

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
      style = AppTheme.typographySystem.caption,
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
  member: ProductionMemberUi,
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
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space4)
      Text(
        text = member.role,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Image(
      painter = painterResource(DesignSystemRes.drawable.ic_chevron_right),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
      contentDescription = null
    )
  }
}

@LightDarkPreview
@Composable
private fun TeamCardPreview() {
  AppTheme {
    TeamCard(
      viewerCrew = ViewerCrewUi(
        viewerRole = "Producer",
        manager = ProductionMemberUi(
          id = "m1",
          name = "Maya Rivera",
          role = "Director",
          initials = "MR",
          avatarColorHex = "#E91E63"
        ),
        peers = persistentListOf(
          ProductionMemberUi(
            id = "m3",
            name = "Sara Lin",
            role = "Cinematographer",
            initials = "SL",
            avatarColorHex = "#9C27B0"
          )
        ),
        reports = persistentListOf(
          ProductionMemberUi(
            id = "m4",
            name = "Jake Morse",
            role = "1st AD",
            initials = "JM",
            avatarColorHex = "#009688"
          ),
          ProductionMemberUi(
            id = "m5",
            name = "Sara Lin",
            role = "Cinematographer",
            initials = "SL",
            avatarColorHex = "#9C27B0"
          )
        )
      )
    )
  }
}
