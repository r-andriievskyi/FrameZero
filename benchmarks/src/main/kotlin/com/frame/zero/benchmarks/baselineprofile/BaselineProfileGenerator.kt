package com.frame.zero.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frame.zero.benchmarks.PACKAGE_NAME
import com.frame.zero.benchmarks.awaitSessionResolved
import com.frame.zero.benchmarks.browseProductions
import com.frame.zero.benchmarks.grantNotificationsPermission
import com.frame.zero.benchmarks.signInIfNeeded
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the baseline profile committed at
 * `androidApp/src/demoRelease/generated/baselineProfiles/`. Runs against the demo flavor
 * (offline seed data, any credentials); prod reuses the result — see the coverage caveat
 * on the `prod` source set in androidApp/build.gradle.kts. Runbook: docs/performance.md.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

  @get:Rule
  val rule = BaselineProfileRule()

  /**
   * Cold-start rules. Deliberately NOT includeInStartupProfile: that flag routes this test's
   * output to a separate startup-prof.txt (an either/or, not an addition) which only buys dex
   * layout optimization while costing ~2.4MB in VCS. Without it the same rules land in the
   * baseline profile, so ART still AOT-compiles them.
   */
  @Test
  fun startup() = rule.collect(packageName = PACKAGE_NAME) {
    grantNotificationsPermission()
    pressHome()
    startActivityAndWait()
    awaitSessionResolved()
  }

  @Test
  fun userJourney() = rule.collect(packageName = PACKAGE_NAME) {
    grantNotificationsPermission()
    pressHome()
    startActivityAndWait()
    signInIfNeeded()
    browseProductions()
  }
}
