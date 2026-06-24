package com.frame.zero.task

import com.frame.zero.config.AppConfig
import com.frame.zero.storage.FileStorage
import com.frame.zero.storage.FilesystemFileStorage
import org.koin.dsl.module

fun taskModule(config: AppConfig) =
  module {
    single<TaskRepository> { TaskRepositoryImpl() }
    single<FileStorage> { FilesystemFileStorage(config.fileStorage.directory) }
    single { TaskService(get(), get(), get(), get(), get(), get()) }
  }
