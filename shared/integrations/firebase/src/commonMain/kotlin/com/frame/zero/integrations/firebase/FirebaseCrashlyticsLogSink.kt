package com.frame.zero.integrations.firebase

import com.frame.zero.core.logging.LogLevel
import com.frame.zero.core.logging.LogSink
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Forwards every log record to Firebase Crashlytics: each record is logged as a breadcrumb,
 * and warnings/errors that carry a [Throwable] are reported as non-fatal exceptions (see
 * [crashlyticsActionFor]).
 *
 * [collectionEnabled] gates whether Crashlytics actually uploads — pass `false` for debug
 * builds so local crashes don't pollute the production console.
 */
class FirebaseCrashlyticsLogSink(
  collectionEnabled: Boolean
) : LogSink {
  private val crashlytics = Firebase.crashlytics

  init {
    crashlytics.setCrashlyticsCollectionEnabled(collectionEnabled)
  }

  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?
  ) {
    crashlytics.log(breadcrumbOf(level, tag, message))
    if (crashlyticsActionFor(level, throwable) == CrashlyticsAction.BreadcrumbAndRecord && throwable != null) {
      crashlytics.recordException(throwable)
    }
  }
}
