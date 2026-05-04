package com.frame.zero.feature.home

import com.frame.zero.feature.home.data.DashboardRepositoryImpl
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import com.frame.zero.repository.dashboard.DashboardRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val featureHomeModule: Module = module {
  single<DashboardRepository> { DashboardRepositoryImpl(get(), get()) }
  factory { GetMeUseCase(get()) }
  factory { GetDashboardUseCase(get()) }
  factory { DashboardTabViewModel(get(), get()) }
}
