package com.frame.zero.di

import com.frame.zero.core.config.BuildFlags
import com.frame.zero.core.upload.BackgroundUrlSessionTaskUploadScheduler
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.demo.DemoBindings
import com.frame.zero.feature.auth.data.AuthRepositoryImpl
import com.frame.zero.feature.auth.data.UserRepositoryImpl
import com.frame.zero.feature.home.data.DashboardRepositoryImpl
import com.frame.zero.feature.home.data.ScheduleRepositoryImpl
import com.frame.zero.integrations.firebase.FirebaseBindings
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
import dev.zacsweers.metro.createGraph

/**
 * The iOS-only accessor on top of [AppGraph]: `IosBackgroundUploadBridge` needs the scheduler
 * before any UI exists.
 */
interface IosAppGraph : AppGraph {
  val taskUploadScheduler: TaskUploadScheduler
}

/** See [AndroidProdGraph][com.frame.zero.di.AndroidProdGraph] for why both sides exclude. */
@DependencyGraph(AppScope::class, excludes = [DemoBindings::class])
interface IosProdGraph : IosAppGraph

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
    BackgroundUrlSessionTaskUploadScheduler::class
  ]
)
interface IosDemoGraph : IosAppGraph

/**
 * One graph per process. iOS has two entry points — the UI and the background-URLSession relaunch
 * in `IosBackgroundUploadBridge` — and a relaunch can reach the bridge before the UI exists, so
 * both must resolve the *same* scheduler instance.
 */
internal object IosGraphHolder {
  val graph: IosAppGraph by lazy {
    if (BuildFlags.IS_DEMO) createGraph<IosDemoGraph>() else createGraph<IosProdGraph>()
  }
}
