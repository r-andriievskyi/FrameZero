package com.frame.zero.domain

import kotlinx.io.IOException

class OfflineException(
  message: String? = null
) : IOException(message)
