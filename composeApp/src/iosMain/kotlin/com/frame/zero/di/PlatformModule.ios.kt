package com.frame.zero.di

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.files.FilePicker
import com.frame.zero.core.files.IosAttachmentFileManager
import com.frame.zero.core.files.IosFilePicker
import com.frame.zero.core.appupdate.IosStoreLauncher
import com.frame.zero.core.appupdate.StoreLauncher
import com.frame.zero.core.config.AppVersionProvider
import com.frame.zero.core.config.IosAppVersionProvider
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.network.connectivity.IosConnectivityObserver
import com.frame.zero.core.security.BiometricAuthenticator
import com.frame.zero.core.security.IosBiometricAuthenticator
import com.frame.zero.core.upload.BackgroundUrlSessionTaskUploadScheduler
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.database.DatabaseBuilderFactory
import com.frame.zero.database.IosDatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
  module {
    single<DatabaseBuilderFactory> { IosDatabaseBuilderFactory() }
    single<ConnectivityObserver> { IosConnectivityObserver() }
    single<AppVersionProvider> { IosAppVersionProvider() }
    single<StoreLauncher> { IosStoreLauncher() }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
    single<FilePicker> { IosFilePicker() }
    single<AttachmentFileManager> { IosAttachmentFileManager() }
    single<TaskUploadScheduler> { BackgroundUrlSessionTaskUploadScheduler(get(), get(), get()) }
  }
