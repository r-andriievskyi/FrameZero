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
 * which artifact (if any) supplies this — `:shared` just consumes whatever it is handed.
 *
 * Unlike the other sinks this is *not* a multibinding: `:androidApp` sits downstream of
 * `:composeApp`, where the graph is declared, so Metro could never aggregate a contribution from
 * there. It arrives as a `Set<HttpDebugInterceptor>` input on the graph factory instead — see
 * `debugHttpInterceptors` in `:androidApp`.
 */
fun interface HttpDebugInterceptor : Interceptor
