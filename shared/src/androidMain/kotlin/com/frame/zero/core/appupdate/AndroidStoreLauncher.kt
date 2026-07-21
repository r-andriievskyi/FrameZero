package com.frame.zero.core.appupdate

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class AndroidStoreLauncher(
  private val context: Context
) : StoreLauncher {
  override fun open(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
  }
}
