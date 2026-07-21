package com.frame.zero.core.config

import platform.Foundation.NSBundle

/**
 * [AppVersionProvider] backed by the main bundle's `Info.plist`. `CFBundleVersion` is the store
 * build number the App Store gates on; `CFBundleShortVersionString` is the display version.
 *
 * **Convention:** `CFBundleVersion` must be a bare integer (Apple also permits dotted forms like
 * `"1.2.3"`, but the force-update policy compares a single monotonic build). If it can't be parsed
 * as an integer — missing, dotted, or malformed — [current] fails **open**: [Int.MAX_VALUE] reads
 * as "newest", so a misconfigured build is never mistaken for an outdated one and hard-gated. This
 * matches [com.frame.zero.repository.app_update.AppUpdateRepository]'s "never a false lockout" rule.
 */
class IosAppVersionProvider : AppVersionProvider {
  override fun current(): AppVersion {
    val info = NSBundle.mainBundle.infoDictionary
    val build = (info?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: Int.MAX_VALUE
    val name = (info?.get("CFBundleShortVersionString") as? String).orEmpty()
    return AppVersion(buildNumber = build, name = name)
  }
}
