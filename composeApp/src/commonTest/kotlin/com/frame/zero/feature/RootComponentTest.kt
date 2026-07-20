package com.frame.zero.feature

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.navigation.DeepLink
import com.frame.zero.core.navigation.NavigationSignal
import com.frame.zero.core.security.AppLockController
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.core.security.BiometricAvailability
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.core.security.BiometricResult
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.User
import com.frame.zero.feature.account.AccountViewModel
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.feature.auth.domain.RegisterUseCase
import com.frame.zero.feature.home.HomeComponent
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.productions.ProductionsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import com.frame.zero.feature.home.usecase.GetScheduleUseCase
import com.frame.zero.feature.production.CreateProductionViewModel
import com.frame.zero.feature.production.details.ProductionDetailsViewModel
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.domain.GetProductionTasksUseCase
import com.frame.zero.feature.production.domain.CreateProductionUseCase
import com.frame.zero.feature.task.details.TaskDetailsViewModel
import com.frame.zero.feature.task.list.TasksListViewModel
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetAssignableMembersUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.feature.task.details.usecase.ObserveTaskChatUnreadUseCase
import com.frame.zero.feature.task.details.usecase.UpdateTaskParticipantsUseCase
import com.frame.zero.testing.FakeAuthRepository
import com.frame.zero.testing.FakeChatRepository
import com.frame.zero.testing.FakeConnectivityObserver
import com.frame.zero.testing.FakeDashboardRepository
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.FakeScheduleRepository
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.FakeUserRepository
import com.frame.zero.testing.NoopSessionAuthOperations
import com.russhwolf.settings.MapSettings
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Covers the only navigation stack in the app: how [RootComponent] swaps the root config
 * on [com.frame.zero.core.session.SessionState] changes, pushes/pops feature screens via the
 * callbacks it hands to children, applies a buffered deep link once logged in, and gates the
 * biometric lock overlay. Children are wired with test-fixtures fakes; the test only asserts
 * on the resulting child stack.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RootComponentTest {
  private val mainDispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() = Dispatchers.setMain(mainDispatcher)

  @AfterTest
  fun tearDown() = Dispatchers.resetMain()

  private val user = User(id = "u1", email = "a@b.c", firstName = "Ada", lastName = "Lovelace")

  @Test
  fun `starts on the splash screen while the session is loading`() =
    runTest(mainDispatcher) {
      val root = makeRoot()
      advanceUntilIdle()

      assertEquals(RootComponent.Config.Splash, root.activeConfig)
      root.tearDown()
    }

  @Test
  fun `a logged-out session shows the auth stack`() =
    runTest(mainDispatcher) {
      val session = session()
      val root = makeRoot(session = session)

      session.initialize() // no tokens -> LoggedOut
      advanceUntilIdle()

      assertEquals(RootComponent.Config.Auth, root.activeConfig)
      root.tearDown()
    }

  @Test
  fun `an authenticated session shows the home stack`() =
    runTest(mainDispatcher) {
      val session = session()
      val root = makeRoot(session = session)

      session.onAuthenticated(user)
      advanceUntilIdle()

      assertEquals(RootComponent.Config.Home, root.activeConfig)
      root.tearDown()
    }

  @Test
  fun `force-logout from a pushed screen returns to a fresh auth stack`() =
    runTest(mainDispatcher) {
      val session = session()
      val root = makeRoot(session = session)
      session.onAuthenticated(user)
      advanceUntilIdle()

      root.homeCallbacks.onProductionClick("p1")
      advanceUntilIdle()
      assertEquals(RootComponent.Config.ProductionDetails("p1"), root.activeConfig)

      session.logout()
      advanceUntilIdle()

      assertEquals(RootComponent.Config.Auth, root.activeConfig)
      // replaceAll wipes the pushed production screen — back stack must be empty.
      assertTrue(root.component.stack.value.backStack.isEmpty())
      root.tearDown()
    }

  @Test
  fun `clicking a production pushes details and back pops to home`() =
    runTest(mainDispatcher) {
      val session = session()
      val root = makeRoot(session = session)
      session.onAuthenticated(user)
      advanceUntilIdle()

      root.homeCallbacks.onProductionClick("p1")
      advanceUntilIdle()
      assertEquals(RootComponent.Config.ProductionDetails("p1"), root.activeConfig)

      val handled = root.backDispatcher.back()
      advanceUntilIdle()

      assertTrue(handled)
      assertEquals(RootComponent.Config.Home, root.activeConfig)
      root.tearDown()
    }

  @Test
  fun `home callbacks push create-production account and task screens`() =
    runTest(mainDispatcher) {
      val session = session()
      val root = makeRoot(session = session)
      session.onAuthenticated(user)
      advanceUntilIdle()

      root.homeCallbacks.onCreateProductionClick()
      advanceUntilIdle()
      assertEquals(RootComponent.Config.CreateProduction, root.activeConfig)
      root.backDispatcher.back()
      advanceUntilIdle()

      root.homeCallbacks.onAccountClick()
      advanceUntilIdle()
      assertEquals(RootComponent.Config.Account, root.activeConfig)
      root.backDispatcher.back()
      advanceUntilIdle()

      root.homeCallbacks.onTaskClick("t1")
      advanceUntilIdle()
      assertEquals(RootComponent.Config.TaskDetails("t1"), root.activeConfig)
      root.tearDown()
    }

  @Test
  fun `a deep link buffered while logged out is applied once logged in`() =
    runTest(mainDispatcher) {
      val session = session()
      val navigationSignal = NavigationSignal()
      val root = makeRoot(session = session, navigationSignal = navigationSignal)

      // Arrives before sign-in (e.g. push tapped on the login screen) — held by replay cache.
      navigationSignal.emit(DeepLink.TaskDetails("t9"))
      session.onAuthenticated(user)
      advanceUntilIdle()

      assertEquals(RootComponent.Config.TaskDetails("t9"), root.activeConfig)
      // The deep link sits on top of the home stack, not instead of it.
      assertEquals(
        RootComponent.Config.Home,
        root.component.stack.value.backStack.last().configuration
      )
      root.tearDown()
    }

  @Test
  fun `the lock overlay is engaged only when logged in and locked`() =
    runTest(mainDispatcher) {
      val session = session()
      val appLock = lockedController()
      val root = makeRoot(session = session, appLockController = appLock)
      advanceUntilIdle()

      // Locked but not yet signed in -> overlay stays down.
      assertFalse(root.component.isLocked.value)

      session.onAuthenticated(user)
      advanceUntilIdle()

      assertTrue(root.component.isLocked.value)
      root.tearDown()
    }

  private fun TestScope.session(): SessionManager =
    SessionManager(
      tokenStorage = TokenStorage(MapSettings()),
      authOperations = NoopSessionAuthOperations,
      userCache = UserCache(MapSettings()),
      logoutSignal = LogoutSignal(),
      scope = backgroundScope
    )

  private fun controller(enabled: Boolean): AppLockController {
    val settings = MapSettings().apply { putBoolean("security.app_lock_enabled", enabled) }
    return AppLockController(AlwaysAvailableAuthenticator, settings)
  }

  private fun lockedController(): AppLockController = controller(enabled = true)

  private fun TestScope.makeRoot(
    session: SessionManager = session(),
    navigationSignal: NavigationSignal = NavigationSignal(),
    appLockController: AppLockController = controller(enabled = false)
  ): RootHandle {
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val context = DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher)
    val productions = FakeProductionsRepository()
    val tasks = FakeTasksRepository()
    val callbacks = HomeCallbacks()

    val component = RootComponent(
      componentContext = context,
      sessionManager = session,
      appLockController = appLockController,
      navigationSignal = navigationSignal,
      authComponentFactory = { ctx ->
        AuthComponent(
          componentContext = ctx,
          signInViewModelFactory = { SignInViewModel(LoginUseCase(FakeAuthRepository(), session)) },
          registerViewModelFactory = { RegisterViewModel(RegisterUseCase(FakeAuthRepository(), session)) }
        )
      },
      homeComponentFactory = { ctx, onCreateProduction, onProduction, onAccount, onTask, onTasks ->
        callbacks.onCreateProductionClick = onCreateProduction
        callbacks.onProductionClick = onProduction
        callbacks.onAccountClick = onAccount
        callbacks.onTaskClick = onTask
        callbacks.onTasksClick = onTasks
        HomeComponent(
          componentContext = ctx,
          dashboardViewModelFactory = {
            DashboardTabViewModel(
              getMeUseCase = GetMeUseCase(FakeUserRepository()),
              getDashboardUseCase = GetDashboardUseCase(FakeDashboardRepository()),
              connectivityObserver = FakeConnectivityObserver()
            )
          },
          productionsViewModelFactory = { ProductionsTabViewModel(productions) },
          scheduleViewModelFactory = {
            ScheduleTabViewModel(
              getScheduleUseCase = GetScheduleUseCase(FakeScheduleRepository()),
              connectivityObserver = FakeConnectivityObserver()
            )
          }
        )
      },
      createProductionViewModelFactory = { CreateProductionViewModel(CreateProductionUseCase(productions)) },
      productionDetailsViewModelFactory = { productionId ->
        ProductionDetailsViewModel(
          productionId = productionId,
          getProductionDetailsUseCase = GetProductionDetailsUseCase(productions),
          getProductionTasksUseCase = GetProductionTasksUseCase(tasks),
          deleteProductionUseCase = DeleteProductionUseCase(productions)
        )
      },
      taskDetailsViewModelFactory = { taskId ->
        TaskDetailsViewModel(
          taskId = taskId,
          getTaskDetailsUseCase = GetTaskDetailsUseCase(tasks),
          completeTaskUseCase = CompleteTaskUseCase(tasks),
          getAssignableMembersUseCase = GetAssignableMembersUseCase(productions),
          updateTaskParticipantsUseCase = UpdateTaskParticipantsUseCase(tasks),
          tasksRepository = tasks,
          observeTaskChatUnreadUseCase = ObserveTaskChatUnreadUseCase(FakeChatRepository()),
          attachmentFileManager = NoopAttachmentFileManager
        )
      },
      // Not navigated to in these tests; a CreateTask push would need the upload graph.
      createTaskViewModelFactory = { _, _ -> error("create-task navigation is not exercised here") },
      // Not navigated to in these tests; a Chat push would need the chat graph.
      chatViewModelFactory = { error("chat navigation is not exercised here") },
      tasksListViewModelFactory = { productionId -> TasksListViewModel(productionId, tasks) },
      accountViewModelFactory = { AccountViewModel(session, appLockController) }
    )
    lifecycle.resume()
    return RootHandle(component, callbacks, backDispatcher, lifecycle)
  }

  private class HomeCallbacks {
    var onCreateProductionClick: () -> Unit = {}
    var onProductionClick: (String) -> Unit = {}
    var onAccountClick: () -> Unit = {}
    var onTaskClick: (String) -> Unit = {}
    var onTasksClick: () -> Unit = {}
  }

  private class RootHandle(
    val component: RootComponent,
    val homeCallbacks: HomeCallbacks,
    val backDispatcher: BackDispatcher,
    private val lifecycle: LifecycleRegistry
  ) {
    val activeConfig: RootComponent.Config
      get() = component.stack.value.active.configuration

    fun tearDown() = lifecycle.destroy()
  }

  private object AlwaysAvailableAuthenticator : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = BiometricAvailability.Available

    override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult = BiometricResult.Success
  }

  private object NoopAttachmentFileManager : AttachmentFileManager {
    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = null

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      channel: ByteReadChannel
    ): String = ""

    override fun delete(localPath: String) = Unit

    override fun openWith(
      localPath: String,
      contentType: String
    ) = Unit

    override fun availableBytes(): Long = Long.MAX_VALUE
  }
}
