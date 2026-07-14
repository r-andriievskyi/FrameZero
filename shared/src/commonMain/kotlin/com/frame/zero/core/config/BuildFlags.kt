package com.frame.zero.core.config

import com.frame.zero.core.network.BuildKonfig

/**
 * Build-flavor flags surfaced to modules outside `shared`, which can't read the internal
 * generated [BuildKonfig] (mirrors the [com.frame.zero.core.network.NetworkConfig.isDebug]
 * trick). [IS_DEMO] is `const` so it constant-folds — the demo wiring in `AppModule` becomes
 * dead code that R8 strips from prod release builds.
 */
object BuildFlags {
  const val IS_DEMO: Boolean = BuildKonfig.DEMO
}
