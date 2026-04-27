package com.frame.zero.di

import com.frame.zero.core.network.networkModule
import com.frame.zero.core.session.sessionModule
import com.frame.zero.feature.auth.authModule
import com.frame.zero.feature.dashboard.featureDashboardModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module

val appModules: List<Module> =
  listOf(networkModule, sessionModule, authModule, featureDashboardModule)

fun initKoin(extraModules: List<Module> = emptyList()): Koin =
  startKoin { modules(appModules + platformModule() + extraModules) }.koin
