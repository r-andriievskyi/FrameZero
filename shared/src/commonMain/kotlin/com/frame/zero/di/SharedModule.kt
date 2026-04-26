package com.frame.zero.di

import com.frame.zero.feature.auth.featureAuthModule
import com.frame.zero.feature.dashboard.featureDashboardModule
import com.frame.zero.repository.auth.repositoryAuthModule
import org.koin.core.module.Module

val sharedModules: List<Module> =
  listOf(repositoryAuthModule, featureAuthModule, featureDashboardModule)
