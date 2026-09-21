package com.frame.zero.core.security

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.lang.ref.WeakReference

/**
 * Tracks the currently-resumed [FragmentActivity] so [AndroidBiometricAuthenticator] can
 * host a `BiometricPrompt` (which requires an activity) without `shared` holding an
 * activity reference statically. A graph singleton, attached to the
 * [Application] lifecycle in `FrameZeroApp`. Held weakly so it never leaks an activity.
 */
@SingleIn(AppScope::class)
@Inject
class ActivityHolder : Application.ActivityLifecycleCallbacks {
  private var currentActivity: WeakReference<FragmentActivity> = WeakReference(null)

  val activity: FragmentActivity?
    get() = currentActivity.get()

  fun attachTo(application: Application) {
    application.registerActivityLifecycleCallbacks(this)
  }

  override fun onActivityResumed(activity: Activity) {
    if (activity is FragmentActivity) currentActivity = WeakReference(activity)
  }

  override fun onActivityPaused(activity: Activity) {
    if (currentActivity.get() === activity) currentActivity = WeakReference(null)
  }

  override fun onActivityCreated(
    activity: Activity,
    savedInstanceState: Bundle?
  ) = Unit

  override fun onActivityStarted(activity: Activity) = Unit

  override fun onActivityStopped(activity: Activity) = Unit

  override fun onActivitySaveInstanceState(
    activity: Activity,
    outState: Bundle
  ) = Unit

  override fun onActivityDestroyed(activity: Activity) = Unit
}
