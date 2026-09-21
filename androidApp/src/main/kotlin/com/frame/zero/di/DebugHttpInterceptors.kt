package com.frame.zero.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.frame.zero.core.network.HttpDebugInterceptor

/**
 * The [HttpDebugInterceptor]s handed to the graph. Whether this actually inspects traffic is
 * decided entirely by which artifact `androidApp/build.gradle.kts` links — `debugImplementation`
 * pulls in the real `ChuckerInterceptor`, `releaseImplementation` pulls in Chucker's own no-op
 * artifact of the same API, so this code needs no debug/release branching itself.
 *
 * This is a graph *input* rather than a contributed multibinding: `:androidApp` sits downstream of
 * `:composeApp`, where the graph is declared, so Metro could never aggregate a contribution from
 * here.
 */
fun debugHttpInterceptors(context: Context): Set<HttpDebugInterceptor> {
  // Built on the first intercepted request, not here: this is called from Application.onCreate,
  // and Chucker's collector opens its own Room database. Under Koin this sat inside a `single { }`
  // lambda and so never ran on the startup path; `by lazy` keeps it off.
  val chucker by lazy { ChuckerInterceptor.Builder(context).build() }
  return setOf(HttpDebugInterceptor { chain -> chucker.intercept(chain) })
}
