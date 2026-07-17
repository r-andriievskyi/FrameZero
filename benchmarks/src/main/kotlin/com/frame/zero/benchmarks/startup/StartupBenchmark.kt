package com.frame.zero.benchmarks.startup

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frame.zero.benchmarks.PACKAGE_NAME
import com.frame.zero.benchmarks.awaitSessionResolved
import com.frame.zero.benchmarks.grantNotificationsPermission
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold-startup benchmark. The delta between [startupWithoutBaselineProfile] and
 * [startupWithBaselineProfile] is what the committed baseline profile buys.
 * `timeToFullDisplayMs` is driven by `ReportDrawnWhen` in MainActivity.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

  @get:Rule
  val rule = MacrobenchmarkRule()

  @Test
  fun startupWithoutBaselineProfile() = startup(CompilationMode.None())

  @Test
  fun startupWithBaselineProfile() =
    startup(CompilationMode.Partial(BaselineProfileMode.Require))

  private fun startup(compilationMode: CompilationMode) = rule.measureRepeated(
    packageName = PACKAGE_NAME,
    metrics = listOf(StartupTimingMetric()),
    compilationMode = compilationMode,
    startupMode = StartupMode.COLD,
    iterations = 10,
    setupBlock = {
      grantNotificationsPermission()
      pressHome()
    }
  ) {
    startActivityAndWait()
    awaitSessionResolved()
  }
}
