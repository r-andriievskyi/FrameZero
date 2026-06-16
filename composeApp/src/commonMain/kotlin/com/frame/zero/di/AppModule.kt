package com.frame.zero.di

import com.frame.zero.core.analytics.analyticsModule
import com.frame.zero.core.logging.loggingModule
import com.frame.zero.core.network.networkModule
import com.frame.zero.core.session.sessionModule
import com.frame.zero.feature.account.featureAccountModule
import com.frame.zero.feature.auth.authModule
import com.frame.zero.feature.home.featureHomeModule
import com.frame.zero.feature.production.details.featureProductionDetailsModule
import com.frame.zero.feature.production.featureProductionModule
import com.frame.zero.feature.task.create.featureTaskCreateModule
import com.frame.zero.feature.task.details.featureTaskDetailsModule
import com.frame.zero.integrations.firebase.firebaseModule
import com.frame.zero.repository.productions.productionsRepositoryModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module

val appModules = listOf(
  loggingModule,
  analyticsModule,
  firebaseModule,
  networkModule,
  sessionModule,
  authModule,
  featureAccountModule,
  featureHomeModule,
  featureProductionModule,
  featureProductionDetailsModule,
  featureTaskDetailsModule,
  featureTaskCreateModule,
  productionsRepositoryModule
)

fun initKoin(extraModules: List<Module> = emptyList()): Koin =
  startKoin {
    modules(appModules + platformModule() + extraModules)
  }.koin
