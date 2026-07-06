package com.frame.zero.feature.account

import com.frame.zero.core.network.NetworkConfig
import org.koin.dsl.module

val featureAccountModule = module {
  factory { AccountViewModel(get(), get(), isDebug = get<NetworkConfig>().isDebug) }
}
