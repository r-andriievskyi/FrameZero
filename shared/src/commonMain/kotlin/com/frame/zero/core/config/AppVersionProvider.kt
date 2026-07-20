package com.frame.zero.core.config

/**
 * Reads the running [AppVersion] from the platform. Interface + platform impls (mirroring
 * [com.frame.zero.core.network.connectivity.ConnectivityObserver]) rather than a BuildKonfig
 * field, so iOS reports its *shipped* `CFBundleVersion` from the Xcode plist — the build the App
 * Store actually knows — instead of a Gradle value that would drift from it.
 */
interface AppVersionProvider {
  fun current(): AppVersion
}
