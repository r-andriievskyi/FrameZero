package com.frame.zero.notification

import org.koin.dsl.module

fun notificationModule() = module {
  single<NotificationRepository> { NotificationRepositoryImpl() }
  single { NotificationService(get()) }
}
