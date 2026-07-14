package com.frame.zero.demo

import com.frame.zero.core.push.PushTokenProvider
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.demo.auth.DemoAuthRepository
import com.frame.zero.demo.auth.DemoUserRepository
import com.frame.zero.demo.data.DemoChatRepository
import com.frame.zero.demo.data.DemoDashboardRepository
import com.frame.zero.demo.data.DemoProductionsRepository
import com.frame.zero.demo.data.DemoScheduleRepository
import com.frame.zero.demo.data.DemoTasksRepository
import com.frame.zero.demo.push.DemoDeviceTokenRepository
import com.frame.zero.demo.push.DemoPushTokenProvider
import com.frame.zero.demo.upload.DemoTaskUploadScheduler
import com.frame.zero.repository.auth.AuthRepository
import com.frame.zero.repository.chat.ChatRepository
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.device_token.DeviceTokenRepository
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.schedule.ScheduleRepository
import com.frame.zero.repository.tasks.TasksRepository
import com.frame.zero.repository.user.UserRepository
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Demo wiring: fake, fully-local implementations of every repository plus the push/upload
 * seams. Loaded LAST in `initKoin` so these definitions override the real ones bound in the
 * feature and platform modules (Koin: last definition wins). Only wired when `BuildFlags.IS_DEMO`.
 */
val demoModule = module {
  single { DemoDataStore() }

  single { DemoAuthRepository(get(), get()) } binds arrayOf(AuthRepository::class, SessionAuthOperations::class)
  single<UserRepository> { DemoUserRepository(get()) }
  single<DashboardRepository> { DemoDashboardRepository(get(), get()) }
  single<ScheduleRepository> { DemoScheduleRepository(get()) }
  single { DemoTasksRepository(get()) } bind TasksRepository::class
  single<ProductionsRepository> { DemoProductionsRepository(get()) }
  single { DemoChatRepository(get()) } bind ChatRepository::class

  single<PushTokenProvider> { DemoPushTokenProvider() }
  single<DeviceTokenRepository> { DemoDeviceTokenRepository() }
  single<TaskUploadScheduler> { DemoTaskUploadScheduler(get()) }

  single { DemoSessionCleaner(get(), get()) } bind SessionCleaner::class
}
