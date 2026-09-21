package com.frame.zero.di

import com.arkivanov.decompose.ComponentContext
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.home.HomeComponent

/**
 * Builds the Decompose root from the graph. Both hosts call this, so the factory wiring exists
 * once rather than being duplicated between `MainActivity` and `MainViewController`.
 *
 * The graph is used here, at the call site — components still receive plain factory lambdas and
 * never see a DI container, per the navigation contract in CLAUDE.md.
 */
fun AppGraph.createRootComponent(componentContext: ComponentContext): RootComponent =
  RootComponent(
    componentContext = componentContext,
    sessionManager = sessionManager,
    appLockController = appLockController,
    forceUpdateController = forceUpdateController,
    navigationSignal = navigationSignal,
    authComponentFactory = { ctx ->
      AuthComponent(
        componentContext = ctx,
        signInViewModelFactory = { signInViewModel },
        registerViewModelFactory = { registerViewModel }
      )
    },
    homeComponentFactory = {
      ctx,
      onCreateProductionClick,
      onProductionClick,
      onAccountClick,
      onTaskClick,
      onTasksClick
      ->
      HomeComponent(
        ctx,
        onAccountClick = onAccountClick,
        onCreateProductionClick = onCreateProductionClick,
        onProductionClick = onProductionClick,
        onTaskClick = onTaskClick,
        onTasksClick = onTasksClick,
        dashboardViewModelFactory = { dashboardTabViewModel },
        productionsViewModelFactory = { productionsTabViewModel },
        scheduleViewModelFactory = { scheduleTabViewModel }
      )
    },
    createProductionViewModelFactory = { createProductionViewModel },
    productionDetailsViewModelFactory = { productionId ->
      productionDetailsViewModelFactory.create(productionId)
    },
    taskDetailsViewModelFactory = { taskId -> taskDetailsViewModelFactory.create(taskId) },
    createTaskViewModelFactory = { productionId, productionTitle ->
      createTaskViewModelFactory.create(productionId, productionTitle)
    },
    tasksListViewModelFactory = { productionId -> tasksListViewModelFactory.create(productionId) },
    accountViewModelFactory = { accountViewModel }
  )
