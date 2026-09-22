package com.frame.zero.core.config

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AndroidAppVersionProvider(
  private val context: Context
) : AppVersionProvider {
  override fun current(): AppVersion {
    val manager = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      manager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
      @Suppress("DEPRECATION")
      manager.getPackageInfo(context.packageName, 0)
    }
    return AppVersion(
      buildNumber = info.longVersionCode.toInt(),
      name = info.versionName.orEmpty()
    )
  }
}
