package com.frame.zero.feature.home

import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.usecase.GetMeUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureHomeModule: Module = module {
  factory { GetMeUseCase(get()) }
  factory { DashboardTabViewModel(get()) }
}
