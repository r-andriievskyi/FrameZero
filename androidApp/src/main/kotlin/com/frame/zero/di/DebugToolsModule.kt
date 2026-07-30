package com.frame.zero.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.frame.zero.core.network.HttpDebugInterceptor
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Binds [HttpDebugInterceptor] to Chucker. Whether this actually inspects traffic is decided
 * entirely by which artifact `androidApp/build.gradle.kts` links — `debugImplementation` pulls
 * in the real `ChuckerInterceptor`, `releaseImplementation` pulls in Chucker's own no-op
 * artifact of the same API, so this code needs no debug/release branching itself.
 */
val debugToolsModule: Module =
  module {
    single {
      val chucker = ChuckerInterceptor.Builder(get()).build()
      HttpDebugInterceptor { chain -> chucker.intercept(chain) }
    } bind HttpDebugInterceptor::class
  }
