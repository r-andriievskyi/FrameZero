package com.frame.zero.core.appupdate

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class IosStoreLauncher : StoreLauncher {
  override fun open(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
  }
}
