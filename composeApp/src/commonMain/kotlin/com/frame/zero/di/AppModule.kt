package com.frame.zero.di

import com.frame.zero.core.analytics.analyticsModule
import com.frame.zero.core.config.BuildFlags
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
import com.frame.zero.feature.chat.featureChatModule
import com.frame.zero.feature.task.create.featureTaskCreateModule
import com.frame.zero.feature.task.details.featureTaskDetailsModule
import com.frame.zero.demo.demoModule
import com.frame.zero.integrations.firebase.firebaseModule
import com.frame.zero.repository.device_token.deviceTokenModule
import com.frame.zero.repository.chat.chatRepositoryModule
import com.frame.zero.repository.productions.productionsRepositoryModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatformTools

private val coreModules = listOf(
  loggingModule,
  analyticsModule,
  performanceModule,
  networkModule,
  databaseModule,
  sessionModule,
  uploadModule,
  securityModule
)

private val featureModules = listOf(
  authModule,
  featureAccountModule,
  featureHomeModule,
  featureProductionModule,
  featureProductionDetailsModule,
  featureTaskDetailsModule,
  featureTaskCreateModule,
  featureChatModule,
  deviceTokenModule
)

private val prodDataModules = listOf(
  firebaseModule,
  productionsRepositoryModule,
  chatRepositoryModule
)

val appModules: List<Module> =
  coreModules + featureModules + if (BuildFlags.IS_DEMO) emptyList() else prodDataModules

// Idempotent: a background iOS relaunch (for URLSession events) can reach this before the
// UI does, and the host apps call it once at startup — returning the running Koin avoids a
// "Koin already started" crash.
fun initKoin(extraModules: List<Module> = emptyList()): Koin =
  KoinPlatformTools.defaultContext().getOrNull() ?: startKoin {
    // demoModule LAST so its bindings override the feature-module repo bindings AND
    // platformModule's TaskUploadScheduler.
    modules(appModules + platformModule() + demoOverrides() + extraModules)
  }.koin

private fun demoOverrides(): List<Module> = if (BuildFlags.IS_DEMO) listOf(demoModule) else emptyList()
