package com.frame.zero.core.di

import com.frame.zero.core.analytics.AnalyticsSink
import com.frame.zero.core.logging.LogSink
import com.frame.zero.core.performance.PerformanceSink
import com.frame.zero.core.session.SessionCleaner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

/**
 * Declares the self-registering plugin extension points so a graph with no contributions for one
 * of them still builds. The telemetry sinks are all contributed by `:shared:integrations:firebase`,
 * which demo builds leave out entirely — so in a demo graph these three sets are legitimately
 * empty. [SessionCleaner] is deliberately *not* listed: every graph has cleaners, and an empty one
 * would mean sign-out silently stopped wiping local state. That only holds because
 * `SessionManager.cleaners` has no default value — a defaulted parameter is an *optional* binding
 * in Metro, which would make the missing set compile rather than fail.
 */
@ContributesTo(AppScope::class)
interface CoreMultibindings {
  @Multibinds(allowEmpty = true)
  val logSinks: Set<LogSink>

  @Multibinds(allowEmpty = true)
  val analyticsSinks: Set<AnalyticsSink>

  @Multibinds(allowEmpty = true)
  val performanceSinks: Set<PerformanceSink>
}
