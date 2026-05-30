package com.frame.zero.feature.home

import com.frame.zero.feature.home.data.DashboardRepositoryImpl
import com.frame.zero.feature.home.data.ScheduleRepositoryImpl
import com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel
import com.frame.zero.feature.home.tab.productions.ProductionsTabViewModel
import com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel
import com.frame.zero.feature.home.usecase.GetDashboardUseCase
import com.frame.zero.feature.home.usecase.GetMeUseCase
import com.frame.zero.feature.home.usecase.GetScheduleUseCase
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.schedule.ScheduleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val featureHomeModule: Module =
  module {
    single<DashboardRepository> { DashboardRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get()) }
    factory { GetMeUseCase(get()) }
    factory { GetDashboardUseCase(get()) }
    factory { GetScheduleUseCase(get()) }
    factory { DashboardTabViewModel(get(), get()) }
    factory { ProductionsTabViewModel(get()) }
    factory { ScheduleTabViewModel(get()) }
  }
