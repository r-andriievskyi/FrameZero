package com.frame.zero.testing

import com.frame.zero.core.config.AppVersion
import com.frame.zero.core.config.AppVersionProvider

/**
 * Controllable [AppVersionProvider] for tests. Set [version] to drive version→UpdateType boundary
 * assertions without touching platform `PackageInfo` / `Info.plist`.
 */
class FakeAppVersionProvider(
  var version: AppVersion = AppVersion(buildNumber = 1, name = "1.0")
) : AppVersionProvider {
  override fun current(): AppVersion = version
}
