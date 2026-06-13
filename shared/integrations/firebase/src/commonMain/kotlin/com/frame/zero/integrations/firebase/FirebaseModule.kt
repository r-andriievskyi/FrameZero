package com.frame.zero.integrations.firebase

import com.frame.zero.core.analytics.AnalyticsSink
import com.frame.zero.core.logging.LogSink
import com.frame.zero.core.network.NetworkConfig
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val firebaseModule: Module =
  module {
    single { FirebaseAnalyticsSink() } bind AnalyticsSink::class
    single { FirebaseCrashlyticsLogSink(collectionEnabled = !get<NetworkConfig>().isDebug) } bind LogSink::class
  }
