package com.frame.zero.di

import com.frame.zero.repository.productions.local.DatabaseBuilderFactory
import com.frame.zero.repository.productions.local.IosDatabaseBuilderFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
  module {
    single<DatabaseBuilderFactory> { IosDatabaseBuilderFactory() }
  }
