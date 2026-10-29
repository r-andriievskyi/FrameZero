package com.frame.zero.di

import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.session.SessionManager
import com.frame.zero.feature.account.AccountViewModel
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.force_update.ForceUpdateController
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.productions.ProductionsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.production.CreateProductionViewModel
import com.frame.zero.feature.production.details.ProductionDetailsViewModel
import com.frame.zero.feature.task.create.CreateTaskViewModel
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import com.frame.zero.feature.task.list.TasksListViewModel
import com.frame.zero.repository.device_token.DeviceTokenSynchronizer
import com.frame.zero.repository.tasks.TasksRepository

/**
 * Everything the app's entry points pull off the object graph.
 *
 * This interface carries no `@DependencyGraph` annotation on purpose: Metro requires the
 * annotated graph to live in a platform source set whenever contributions come from both common
 * and platform code, which is our case (database builders, biometrics, the upload schedulers…).
 * The concrete graphs therefore live in `androidMain`/`iosMain` and implement this, one pair per
 * flavor — see `AndroidAppGraphs.kt` and `IosAppGraphs.kt`.
 *
 * Accessors for unscoped types hand back a fresh instance per call, which is what the
 * `() -> ViewModel` factories the Decompose components take expect.
 */
interface AppGraph {
  val sessionManager: SessionManager
  val appLockController: AppLockController
  val forceUpdateController: ForceUpdateController
  val navigationSignal: NavigationSignal

  /**
   * Eagerly resolved at startup by each host: it has to start observing the session when the app
   * launches, not when something first injects it. (Koin expressed this as `createdAtStart`.)
   */
  val deviceTokenSynchronizer: DeviceTokenSynchronizer

  val signInViewModel: SignInViewModel
  val registerViewModel: RegisterViewModel
  val dashboardTabViewModel: DashboardTabViewModel
  val productionsTabViewModel: ProductionsTabViewModel
  val scheduleTabViewModel: ScheduleTabViewModel
  val createProductionViewModel: CreateProductionViewModel
  val accountViewModel: AccountViewModel

  /**
   * Exposed so a graph test can assert which flavor's implementation a build actually got —
   * nothing in the app resolves a repository straight off the graph.
   */
  val tasksRepository: TasksRepository

  val productionDetailsViewModelFactory: ProductionDetailsViewModel.Factory
  val taskDetailsViewModelFactory: TaskDetailsViewModel.Factory
  val createTaskViewModelFactory: CreateTaskViewModel.Factory
  val tasksListViewModelFactory: TasksListViewModel.Factory
}
