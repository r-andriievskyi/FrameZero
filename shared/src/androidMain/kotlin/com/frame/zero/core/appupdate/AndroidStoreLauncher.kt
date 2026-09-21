package com.frame.zero.core.appupdate

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AndroidStoreLauncher(
  private val context: Context
) : StoreLauncher {
  override fun open(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
  }
}
