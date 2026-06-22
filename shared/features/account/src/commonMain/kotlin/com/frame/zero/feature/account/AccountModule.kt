package com.frame.zero.feature.account

import org.koin.dsl.module

val featureAccountModule = module {
  factory { AccountViewModel(get(), get()) }
}
