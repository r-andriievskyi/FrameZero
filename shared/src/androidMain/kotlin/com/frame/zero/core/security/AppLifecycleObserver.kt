package com.frame.zero.core.security

import android.app.Activity
import android.app.Application
import android.os.Bundle

class AppLifecycleObserver(
  private val appLockController: AppLockController
) : Application.ActivityLifecycleCallbacks {
  private var startedActivities = 0

  fun attachTo(application: Application) {
    application.registerActivityLifecycleCallbacks(this)
  }

  override fun onActivityStarted(activity: Activity) {
    startedActivities++
  }

  override fun onActivityStopped(activity: Activity) {
    startedActivities--
    // all activities stopped and this stop is not part of a config-change recreation:
    // the app actually went to the background.
    if (startedActivities <= 0 && !activity.isChangingConfigurations) {
      appLockController.onBackgrounded()
    }
  }

  override fun onActivityCreated(
    activity: Activity,
    savedInstanceState: Bundle?
  ) = Unit

  override fun onActivityResumed(activity: Activity) = Unit

  override fun onActivityPaused(activity: Activity) = Unit

  override fun onActivitySaveInstanceState(
    activity: Activity,
    outState: Bundle
  ) = Unit

  override fun onActivityDestroyed(activity: Activity) = Unit
}
