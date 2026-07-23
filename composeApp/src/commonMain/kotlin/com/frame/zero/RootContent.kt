package com.frame.zero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.account.ui.AccountScreen
import com.frame.zero.feature.auth.ui.AuthScreen
import com.frame.zero.feature.chat.ui.ChatScreen
import com.frame.zero.feature.gallery.ui.DesignSystemGalleryScreen
import com.frame.zero.feature.home.ui.HomeContent
import com.frame.zero.feature.app_update.AppUpdateState
import com.frame.zero.feature.app_update.HardUpdateScreen
import com.frame.zero.feature.app_update.SoftUpdateScreen
import com.frame.zero.feature.lock.BiometricLockOverlay
import com.frame.zero.feature.production.details.ui.ProductionDetailsScreen
import com.frame.zero.feature.production.ui.CreateProductionScreen
import com.frame.zero.feature.splash.PlatformSplash
import com.frame.zero.feature.task.create.ui.CreateTaskScreen
import com.frame.zero.feature.task.details.ui.TaskDetailsScreen
import com.frame.zero.feature.task.list.ui.TasksListScreen

@Composable
fun RootContent(component: RootComponent) {
  val isLocked by component.isLocked.collectAsStateWithLifecycle()
  val updateState by component.updateState.collectAsStateWithLifecycle()
  Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background)) {
    Children(
      stack = component.stack,
      animation = stackAnimation()
    ) { child ->
      when (val instance = child.instance) {
        RootComponent.Child.Splash -> PlatformSplash()
        is RootComponent.Child.Auth -> AuthScreen(instance.component)
        is RootComponent.Child.Home -> HomeContent(instance.component)
        is RootComponent.Child.Account -> AccountScreen(instance.component)
        is RootComponent.Child.Gallery -> DesignSystemGalleryScreen(instance.component)
        is RootComponent.Child.CreateProduction -> CreateProductionScreen(instance.component)
        is RootComponent.Child.ProductionDetails -> ProductionDetailsScreen(instance.component)
        is RootComponent.Child.TaskDetails -> TaskDetailsScreen(instance.component)
        is RootComponent.Child.CreateTask -> CreateTaskScreen(instance.component)
        is RootComponent.Child.Chat -> ChatScreen(instance.component)
        is RootComponent.Child.TasksList -> TasksListScreen(instance.component)
      }
    }
    if (isLocked) {
      BiometricLockOverlay(
        onUnlock = component::unlock,
        onSignOut = component::onLockSignOut
      )
    }
    when (val update = updateState) {
      is AppUpdateState.Hard -> HardUpdateScreen(
        message = update.message,
        onUpdate = component::onUpdateClick
      )
      is AppUpdateState.Soft -> SoftUpdateScreen(
        message = update.message,
        onUpdate = component::onUpdateClick,
        onDismiss = component::onSoftUpdateDismiss
      )
      AppUpdateState.None -> Unit
    }
  }
}
