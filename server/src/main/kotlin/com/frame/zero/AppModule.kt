package com.frame.zero

import com.frame.zero.repository.NotificationRepository
import com.frame.zero.repository.NotificationRepositoryExposed
import com.frame.zero.repository.ProductionMemberRepository
import com.frame.zero.repository.ProductionMemberRepositoryExposed
import com.frame.zero.repository.ProductionRepository
import com.frame.zero.repository.ProductionRepositoryExposed
import com.frame.zero.repository.ScheduleEventRepository
import com.frame.zero.repository.ScheduleEventRepositoryExposed
import com.frame.zero.repository.TaskRepository
import com.frame.zero.repository.TaskRepositoryExposed
import com.frame.zero.repository.UserRepository
import com.frame.zero.services.DashboardService
import com.frame.zero.services.NotificationService
import com.frame.zero.services.ProductionAccessService
import com.frame.zero.services.ProductionService
import com.frame.zero.services.ScheduleService
import com.frame.zero.services.TaskService
import org.koin.dsl.module

fun appModule() =
  module {
    single<ProductionRepository> { ProductionRepositoryExposed() }
    single<ProductionMemberRepository> { ProductionMemberRepositoryExposed() }
    single<TaskRepository> { TaskRepositoryExposed() }
    single<ScheduleEventRepository> { ScheduleEventRepositoryExposed() }
    single<NotificationRepository> { NotificationRepositoryExposed() }
    single { ProductionAccessService(get(), get()) }
    single { ProductionService(get(), get(), get<UserRepository>(), get()) }
    single { DashboardService(get<UserRepository>(), get(), get(), get()) }
    single { TaskService(get(), get()) }
    single { ScheduleService(get(), get(), get()) }
    single { NotificationService(get()) }
  }
