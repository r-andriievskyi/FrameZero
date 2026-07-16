package com.frame.zero.benchmarks

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

internal const val PACKAGE_NAME = "com.frame.zero"

private const val UI_TIMEOUT_MS = 10_000L

// Compose testTags surfaced as resource-ids via testTagsAsResourceId in MainActivity —
// same anchors the Maestro flows in .maestro/ use.
private const val SIGN_IN_EMAIL = "sign-in:email"
private const val SIGN_IN_PASSWORD = "sign-in:password"
private const val SIGN_IN_SUBMIT = "sign-in:submit"
private const val DASHBOARD_GREETING = "dashboard:greeting"
private const val PRODUCTIONS_LIST = "productions:list"
private const val PRODUCTION_DETAILS = "production-details:content"

private const val SEED_PRODUCTION_TITLE = "Neon Tide"

internal fun MacrobenchmarkScope.grantNotificationsPermission() {
  device.executeShellCommand("pm grant $PACKAGE_NAME android.permission.POST_NOTIFICATIONS")
}

internal fun MacrobenchmarkScope.awaitSessionResolved() {
  val resolved = device.wait(
    Until.hasObject(By.res(Pattern.compile("$SIGN_IN_EMAIL|$DASHBOARD_GREETING"))),
    UI_TIMEOUT_MS
  )
  check(resolved) { "Neither sign-in nor dashboard appeared within ${UI_TIMEOUT_MS}ms" }
}

internal fun MacrobenchmarkScope.signInIfNeeded() {
  awaitSessionResolved()
  device.findObject(By.res(SIGN_IN_EMAIL))?.let { email ->
    email.text = "demo@framezero.app"
    device.findObject(By.res(SIGN_IN_PASSWORD)).text = "password"
    device.findObject(By.res(SIGN_IN_SUBMIT)).click()
  }
  check(device.wait(Until.hasObject(By.res(DASHBOARD_GREETING)), UI_TIMEOUT_MS)) {
    "Dashboard did not appear after sign-in"
  }
}

internal fun MacrobenchmarkScope.browseProductions() {
  device.findObject(By.text("Productions")).click()
  check(device.wait(Until.hasObject(By.res(PRODUCTIONS_LIST)), UI_TIMEOUT_MS)) {
    "Productions list did not appear"
  }
  device.findObject(By.res(PRODUCTIONS_LIST)).apply {
    setGestureMargin(device.displayWidth / 5)
    fling(Direction.DOWN)
    device.waitForIdle()
    fling(Direction.UP)
  }
  device.waitForIdle()
  device.findObject(By.text(SEED_PRODUCTION_TITLE)).click()
  check(device.wait(Until.hasObject(By.res(PRODUCTION_DETAILS)), UI_TIMEOUT_MS)) {
    "Production details did not appear"
  }
  device.pressBack()
  check(device.wait(Until.hasObject(By.res(PRODUCTIONS_LIST)), UI_TIMEOUT_MS)) {
    "Productions list did not reappear after back"
  }
}
