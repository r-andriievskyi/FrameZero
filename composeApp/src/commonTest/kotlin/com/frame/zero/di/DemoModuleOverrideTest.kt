package com.frame.zero.di

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.core.session.UserCache
import com.frame.zero.core.upload.PendingTaskUpload
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.demo.demoModule
import com.frame.zero.feature.auth.authModule
import com.frame.zero.repository.auth.AuthRepository
import com.russhwolf.settings.MapSettings
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DemoModuleOverrideTest {
  @AfterTest
  fun tearDown() = stopKoin()

  @Test
  fun demo_module_overrides_feature_and_platform_bindings() {
    val settings = MapSettings()
    // Stands in for sessionModule + platformModule: the deps demoModule needs, plus a "real"
    // scheduler binding that demoModule must override.
    val stubPlatform = module {
      single { TokenStorage(settings) }
      single { UserCache(settings) }
      single<TaskUploadScheduler> { RealSchedulerStub }
    }

    val koin = startKoin { modules(authModule, stubPlatform, demoModule) }.koin

    assertEquals("DemoAuthRepository", koin.get<AuthRepository>()::class.simpleName)
    assertEquals("DemoAuthRepository", koin.get<SessionAuthOperations>()::class.simpleName)
    assertEquals("DemoTaskUploadScheduler", koin.get<TaskUploadScheduler>()::class.simpleName)
  }

  private object RealSchedulerStub : TaskUploadScheduler {
    override suspend fun enqueue(upload: PendingTaskUpload) = Unit

    override suspend fun retry(uploadId: String) = Unit

    override suspend fun cancel(uploadId: String) = Unit
  }
}
