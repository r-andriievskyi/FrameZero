package com.frame.zero.di

import android.content.Context
import com.frame.zero.core.files.AndroidAttachmentFileManager
import com.frame.zero.core.files.AndroidFilePicker
import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.files.FilePicker
import com.frame.zero.core.network.connectivity.AndroidConnectivityObserver
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.security.ActivityHolder
import com.frame.zero.core.security.AndroidBiometricAuthenticator
import com.frame.zero.core.security.AppLifecycleObserver
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.core.upload.WorkManagerTaskUploadScheduler
import com.frame.zero.push.PushNotificationsRouter
import com.frame.zero.database.AndroidDatabaseBuilderFactory
import com.frame.zero.database.DatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformModule(): Module =
  module {
    single<DatabaseBuilderFactory> { AndroidDatabaseBuilderFactory(get()) }
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
    single { PushNotificationsRouter(get()) }
    single { ActivityHolder() }
    single { AppLifecycleObserver(get()) }
    single<BiometricAuthenticator> { AndroidBiometricAuthenticator(get()) }
    single { AndroidFilePicker(get()) } bind FilePicker::class
    single<AttachmentFileManager> { AndroidAttachmentFileManager(get()) }
    single<TaskUploadScheduler> { WorkManagerTaskUploadScheduler(get(), get()) }
  }

fun androidContextModule(context: Context): Module = module { single<Context> { context } }
