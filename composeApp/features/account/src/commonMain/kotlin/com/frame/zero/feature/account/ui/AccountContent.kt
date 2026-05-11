package com.frame.zero.feature.account.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.feature.account.AccountComponent
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_chevron_left
import framezero.composeapp.features.account.generated.resources.ic_chevron_right
import framezero.composeapp.features.account.generated.resources.ic_info
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_team
import framezero.composeapp.features.account.generated.resources.ic_user
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val AvatarSize = 56.dp
private val IconContainerSize = 40.dp
private val IconSize = 20.dp
private val ChevronSize = 20.dp
private val BadgePaddingHorizontal = 10.dp
private val BadgePaddingVertical = 4.dp
private val TopBarHeight = 56.dp
private val SignOutBorderWidth = 1.dp
private val SignOutHeight = 52.dp
private val DividerIndentStart = 72.dp

@Composable
fun AccountContent(component: AccountComponent, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    SettingsTopBar(onBack = component.onBack)
    HorizontalDivider(color = AppTheme.colorSystem.border)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = AppTheme.spacingSystem.space16)
    ) {
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space16))
      ProfileCard()
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = "ACCOUNT") {
        SettingsRow(
          icon = Res.drawable.ic_user,
          title = "Edit profile",
          subtitle = "Maya Rivera"
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_mail,
          title = "Email address",
          subtitle = "maya@studiozero.co"
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_lock,
          title = "Password & security",
          subtitle = "Last changed 3 months ago"
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = "WORKSPACE") {
        SettingsRow(
          icon = Res.drawable.ic_bell,
          title = "Notifications",
          subtitle = "All enabled"
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = "APP") {
        SettingsRow(
          icon = Res.drawable.ic_info,
          title = "About FrameZero",
          subtitle = "Version 3.0.0"
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SignOutButton(onClick = { /* TODO: wire to sign-out action */ })
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
    }
  }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(TopBarHeight)
      .padding(horizontal = AppTheme.spacingSystem.space8),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onBack) {
      Image(
        painter = painterResource(Res.drawable.ic_chevron_left),
        contentDescription = "Back",
        colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
        modifier = Modifier.size(ChevronSize)
      )
    }
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
    Text(
      text = "Settings",
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Composable
private fun ProfileCard(modifier: Modifier = Modifier) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground, shape)
      .border(SignOutBorderWidth, AppTheme.colorSystem.cardBorder, shape)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(AvatarSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(AppTheme.colorSystem.accent),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "MR",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textOnAccent,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space16))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "Maya Rivera",
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space2))
      Text(
        text = "Director · Studio Zero",
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@Composable
private fun SettingsSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textMuted,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(bottom = AppTheme.spacingSystem.space8)
    )
    val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(AppTheme.colorSystem.cardBackground, shape)
        .border(SignOutBorderWidth, AppTheme.colorSystem.cardBorder, shape)
    ) {
      content()
    }
  }
}

@Composable
private fun SettingsRow(
  icon: DrawableResource,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {}
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithRipple(
        color = AppTheme.colorSystem.accentDim,
        bounded = true,
        onClick = onClick
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(IconContainerSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(AppTheme.colorSystem.inputBackground),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(icon),
        contentDescription = title,
        colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent),
        modifier = Modifier.size(IconSize)
      )
    }
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space16))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textPrimary,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space2))
      Text(
        text = subtitle,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Image(
      painter = painterResource(Res.drawable.ic_chevron_right),
      contentDescription = null,
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textMuted),
      modifier = Modifier.size(ChevronSize)
    )
  }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
  HorizontalDivider(
    modifier = modifier.padding(start = DividerIndentStart),
    color = AppTheme.colorSystem.cardBorder
  )
}

@Composable
private fun SignOutButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(SignOutHeight)
      .clip(shape)
      .border(SignOutBorderWidth, AppTheme.colorSystem.errorText, shape)
      .clickableWithRipple(
        color = AppTheme.colorSystem.errorSurface,
        bounded = true,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "Sign out",
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.errorText,
      fontWeight = FontWeight.Medium
    )
  }
}

@Preview
@Composable
private fun AccountContentPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .verticalScroll(rememberScrollState())
        .padding(AppTheme.spacingSystem.space16)
    ) {
      SettingsTopBar(onBack = {})
      HorizontalDivider(color = AppTheme.colorSystem.border)
      Column(modifier = Modifier.padding(horizontal = AppTheme.spacingSystem.space16)) {
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space16))
        ProfileCard()
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = "ACCOUNT") {
          SettingsRow(
            icon = Res.drawable.ic_user,
            title = "Edit profile",
            subtitle = "Maya Rivera"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_mail,
            title = "Email address",
            subtitle = "maya@studiozero.co"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_lock,
            title = "Password & security",
            subtitle = "Last changed 3 months ago"
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = "WORKSPACE") {
          SettingsRow(
            icon = Res.drawable.ic_bell,
            title = "Notifications",
            subtitle = "All enabled"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_team,
            title = "Team & permissions",
            subtitle = "12 members"
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = "APP") {
          SettingsRow(
            icon = Res.drawable.ic_info,
            title = "About FrameZero",
            subtitle = "Version 3.0.0"
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SignOutButton(onClick = {})
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      }
    }
  }
}
