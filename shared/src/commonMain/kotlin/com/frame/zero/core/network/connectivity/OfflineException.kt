package com.frame.zero.core.network.connectivity

import kotlinx.io.IOException

class OfflineException(
  message: String = "No internet connection"
) : IOException(message)
