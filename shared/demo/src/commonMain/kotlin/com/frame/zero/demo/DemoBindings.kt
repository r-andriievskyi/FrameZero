package com.frame.zero.demo

import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.demo.auth.DemoAuthRepository
import com.frame.zero.demo.auth.DemoUserRepository
import com.frame.zero.demo.data.DemoDashboardRepository
import com.frame.zero.demo.data.DemoForceUpdateRepository
import com.frame.zero.demo.data.DemoProductionsRepository
import com.frame.zero.demo.data.DemoScheduleRepository
import com.frame.zero.demo.data.DemoTasksRepository
import com.frame.zero.demo.push.DemoDeviceTokenRepository
import com.frame.zero.demo.push.DemoPushTokenProvider
import com.frame.zero.demo.upload.DemoTaskUploadScheduler
import com.frame.zero.repository.auth.AuthRepository
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.device_token.DeviceTokenRepository
import com.frame.zero.repository.force_update.ForceUpdateRepository
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.schedule.ScheduleRepository
import com.frame.zero.repository.tasks.TasksRepository
import com.frame.zero.repository.user.UserRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Demo wiring: fake, fully-local implementations of every repository plus the push and upload
 * seams, all backed by [DemoDataStore].
 *
 * This is a container rather than per-class `@ContributesBinding` annotations because every fake
 * is `internal` to this module, and Metro cannot see another module's internal declarations when
 * it merges the graph. Bundling them also means the production graphs drop the whole demo side
 * with a single `excludes` entry.
 *
 * Nothing here replaces the real bindings: both sides contribute to `AppScope`, so a graph that
 * forgets to exclude one side fails to compile with a duplicate binding rather than silently
 * shipping fakes.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object DemoBindings {
  @Provides
  @SingleIn(AppScope::class)
  fun demoDataStore(): DemoDataStore = DemoDataStore()

  @Provides
  @SingleIn(AppScope::class)
  fun demoAuthRepository(
    tokenStorage: TokenStorage,
    userCache: UserCache
  ): DemoAuthRepository = DemoAuthRepository(tokenStorage, userCache)

  @Provides
  fun authRepository(demo: DemoAuthRepository): AuthRepository = demo

  @Provides
  fun sessionAuthOperations(demo: DemoAuthRepository): SessionAuthOperations = demo

  @Provides
  @SingleIn(AppScope::class)
  fun userRepository(userCache: UserCache): UserRepository = DemoUserRepository(userCache)

  @Provides
  @SingleIn(AppScope::class)
  fun dashboardRepository(
    store: DemoDataStore,
    userCache: UserCache
  ): DashboardRepository = DemoDashboardRepository(store, userCache)

  @Provides
  @SingleIn(AppScope::class)
  fun scheduleRepository(store: DemoDataStore): ScheduleRepository = DemoScheduleRepository(store)

  @Provides
  @SingleIn(AppScope::class)
  fun demoTasksRepository(store: DemoDataStore): DemoTasksRepository = DemoTasksRepository(store)

  @Provides
  fun tasksRepository(demo: DemoTasksRepository): TasksRepository = demo

  @Provides
  @SingleIn(AppScope::class)
  fun productionsRepository(store: DemoDataStore): ProductionsRepository = DemoProductionsRepository(store)

  @Provides
  @SingleIn(AppScope::class)
  fun forceUpdateRepository(): ForceUpdateRepository = DemoForceUpdateRepository()

  @Provides
  @SingleIn(AppScope::class)
  fun pushTokenProvider(): PushTokenProvider = DemoPushTokenProvider()

  @Provides
  @SingleIn(AppScope::class)
  fun deviceTokenRepository(): DeviceTokenRepository = DemoDeviceTokenRepository()

  @Provides
  @SingleIn(AppScope::class)
  fun taskUploadScheduler(tasksRepository: DemoTasksRepository): TaskUploadScheduler =
    DemoTaskUploadScheduler(tasksRepository)

  @Provides
  @IntoSet
  @SingleIn(AppScope::class)
  fun demoSessionCleaner(store: DemoDataStore): SessionCleaner = DemoSessionCleaner(store)
}
