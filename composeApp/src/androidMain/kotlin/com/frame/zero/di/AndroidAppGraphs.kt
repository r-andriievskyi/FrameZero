package com.frame.zero.di

import android.content.Context
import com.frame.zero.core.config.BuildFlags
import com.frame.zero.core.files.AndroidFilePicker
import com.frame.zero.core.network.HttpDebugInterceptor
import com.frame.zero.core.security.ActivityHolder
import com.frame.zero.core.security.AppLifecycleObserver
import com.frame.zero.core.upload.TaskUploadWorkerFactory
import com.frame.zero.core.upload.WorkManagerTaskUploadScheduler
import com.frame.zero.demo.DemoBindings
import com.frame.zero.feature.auth.data.AuthRepositoryImpl
import com.frame.zero.feature.auth.data.UserRepositoryImpl
import com.frame.zero.feature.home.data.DashboardRepositoryImpl
import com.frame.zero.feature.home.data.ScheduleRepositoryImpl
import com.frame.zero.integrations.firebase.FirebaseBindings
import com.frame.zero.push.PushNotificationsRouter
import com.frame.zero.repository.device_token.DeviceTokenRepositoryImpl
import com.frame.zero.repository.force_update.RemoteConfigForceUpdateRepository
import com.frame.zero.repository.productions.ProductionsRepositoryImpl
import com.frame.zero.repository.productions.ProductionsSessionCleaner
import com.frame.zero.repository.productions.network.ProductionsApiImpl
import com.frame.zero.repository.tasks.TasksRepositoryImpl
import com.frame.zero.repository.tasks.TasksSessionCleaner
import com.frame.zero.repository.tasks.network.TasksApiImpl
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

/** The Android-only accessors on top of [AppGraph], read by `FrameZeroApp` and `MainActivity`. */
interface AndroidAppGraph : AppGraph {
  val activityHolder: ActivityHolder
  val appLifecycleObserver: AppLifecycleObserver
  val pushNotificationsRouter: PushNotificationsRouter
  val filePicker: AndroidFilePicker
  val taskUploadWorkerFactory: TaskUploadWorkerFactory
}

/**
 * The real app. Excludes [DemoBindings] — the demo fakes are on the compile classpath of every
 * build, and both sides contribute to `AppScope`, so leaving this out is a duplicate-binding
 * compile error rather than a production build that quietly serves fake data.
 */
@DependencyGraph(AppScope::class, excludes = [DemoBindings::class])
interface AndroidProdGraph : AndroidAppGraph {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides context: Context,
      @Provides debugInterceptors: Set<HttpDebugInterceptor>
    ): AndroidProdGraph
  }
}

/**
 * The offline demo build: no backend, no Firebase, no WorkManager uploads. The mirror image of
 * [AndroidProdGraph] — it excludes every binding [DemoBindings] supplies its own version of.
 */
@DependencyGraph(
  AppScope::class,
  excludes = [
    AuthRepositoryImpl::class,
    UserRepositoryImpl::class,
    DashboardRepositoryImpl::class,
    ScheduleRepositoryImpl::class,
    ProductionsApiImpl::class,
    ProductionsRepositoryImpl::class,
    ProductionsSessionCleaner::class,
    TasksApiImpl::class,
    TasksRepositoryImpl::class,
    TasksSessionCleaner::class,
    // DeviceTokenSessionCleaner is NOT excluded: demo swaps the repository under it, so the
    // real sign-out path still runs (against no-op fakes) exactly as it does in production.
    DeviceTokenRepositoryImpl::class,
    RemoteConfigForceUpdateRepository::class,
    FirebaseBindings::class,
    WorkManagerTaskUploadScheduler::class
  ]
)
interface AndroidDemoGraph : AndroidAppGraph {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides context: Context,
      @Provides debugInterceptors: Set<HttpDebugInterceptor>
    ): AndroidDemoGraph
  }
}

/**
 * [BuildFlags.IS_DEMO] is a compile-time constant, so R8 folds this and strips the branch the
 * build did not choose.
 */
fun createAndroidAppGraph(
  context: Context,
  debugInterceptors: Set<HttpDebugInterceptor>
): AndroidAppGraph =
  if (BuildFlags.IS_DEMO) {
    createGraphFactory<AndroidDemoGraph.Factory>().create(context, debugInterceptors)
  } else {
    createGraphFactory<AndroidProdGraph.Factory>().create(context, debugInterceptors)
  }
