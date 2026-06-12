package com.frame.zero.feature.home

/**
 * Distinguishes a load failure that the UI should communicate differently:
 *
 * - [Network] — the device is offline. The UI shows a clear "no connection" message
 *   with **no** retry button; the ViewModel re-loads automatically once connectivity
 *   returns.
 * - [Generic] — any other failure (server error, deserialization, …). The UI offers a
 *   manual retry, since coming back online won't fix it.
 */
enum class LoadErrorKind {
  Network,
  Generic
}
