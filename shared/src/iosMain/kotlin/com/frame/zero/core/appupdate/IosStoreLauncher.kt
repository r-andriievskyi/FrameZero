package com.frame.zero.core.appupdate

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosStoreLauncher : StoreLauncher {
  override fun open(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
  }
}
