package com.frame.zero.core.network

import com.frame.zero.SERVER_PORT

data class NetworkConfig(
  val baseUrl: String
) {
  companion object {
    fun localhost(): NetworkConfig = NetworkConfig(baseUrl = "http://10.0.2.2:$SERVER_PORT")
  }
}
