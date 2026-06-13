package com.frame.zero.integrations.firebase

import com.frame.zero.core.analytics.AnalyticsEvent
import com.frame.zero.core.analytics.AnalyticsSink
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Forwards analytics events to Firebase Analytics. [identify] also stamps the Crashlytics
 * user id so crashes can be correlated to the same user as the analytics session.
 */
class FirebaseAnalyticsSink : AnalyticsSink {
  override fun track(event: AnalyticsEvent) {
    Firebase.analytics.logEvent(event.name, event.params)
  }

  override fun identify(userId: String?) {
    Firebase.analytics.setUserId(userId)
    userId?.let { Firebase.crashlytics.setUserId(it) }
  }
}
