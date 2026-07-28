package com.frame.zero.core.network

import okhttp3.Interceptor

/**
 * Self-registering extension point for debug-only OkHttp interceptors (e.g. a network
 * inspector) — mirrors the [com.frame.zero.core.logging.LogSink] / AnalyticsSink pattern.
 *
 * `:shared` has no debug/release build-type split (a single Gradle-invocation task name like
 * "assembleRelease" is not a reliable signal here — see the release gate this replaces), so it
 * can never itself know whether it is linked into a debug or release APK. Only `:androidApp`
 * has real AGP build types, so it alone decides via `debugImplementation`/`releaseImplementation`
 * which artifact (if any) binds this and registers it with Koin — `:shared` just collects
 * whatever was bound. Register one with `single { MyInterceptor() } bind
 * HttpDebugInterceptor::class` from a debug-only dependency.
 */
fun interface HttpDebugInterceptor : Interceptor
