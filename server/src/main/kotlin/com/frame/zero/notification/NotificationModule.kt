package com.frame.zero.notification

import com.frame.zero.config.AppConfig
import org.koin.dsl.module

fun notificationModule(config: AppConfig) =
  module {
    single<NotificationRepository> { NotificationRepositoryImpl() }
    single { NotificationService(get(), get()) }
    single<DeviceTokenRepository> { DeviceTokenRepositoryImpl() }
    single { DeviceTokenService(get(), get()) }
    single<PushSender> { FirebaseAdminPushSender(config.firebase.credentialsPath) }
    single { TaskAssignmentNotifier(get(), get()) }
  }
