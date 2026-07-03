package com.frame.zero.di

import com.frame.zero.core.analytics.analyticsModule
import com.frame.zero.core.logging.loggingModule
import com.frame.zero.core.network.networkModule
import com.frame.zero.core.performance.performanceModule
import com.frame.zero.core.security.securityModule
import com.frame.zero.core.session.sessionModule
import com.frame.zero.core.upload.uploadModule
import com.frame.zero.database.databaseModule
import com.frame.zero.feature.account.featureAccountModule
import com.frame.zero.feature.auth.authModule
import com.frame.zero.feature.home.featureHomeModule
import com.frame.zero.feature.production.details.featureProductionDetailsModule
import com.frame.zero.feature.production.featureProductionModule
import com.frame.zero.feature.task.create.featureTaskCreateModule
import com.frame.zero.feature.task.details.featureTaskDetailsModule
import com.frame.zero.integrations.firebase.firebaseModule
import com.frame.zero.repository.device_token.deviceTokenModule
import com.frame.zero.repository.productions.productionsRepositoryModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

val appModules = listOf(
  loggingModule,
  analyticsModule,
  performanceModule,
  firebaseModule,
  networkModule,
  databaseModule,
  sessionModule,
  uploadModule,
  securityModule,
  authModule,
  featureAccountModule,
  featureHomeModule,
  featureProductionModule,
  featureProductionDetailsModule,
  featureTaskDetailsModule,
  featureTaskCreateModule,
  productionsRepositoryModule,
  deviceTokenModule
)

// Idempotent: a background iOS relaunch (for URLSession events) can reach this before the
// UI does, and the host apps call it once at startup — returning the running Koin avoids a
// "Koin already started" crash.
fun initKoin(extraModules: List<Module> = emptyList()): Koin =
  KoinPlatformTools.defaultContext().getOrNull() ?: startKoin {
    modules(appModules + platformModule() + extraModules)
  }.koin
