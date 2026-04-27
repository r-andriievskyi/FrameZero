package com.frame.zero.feature.auth

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.feature.auth.data.AuthRepositoryImpl
import com.frame.zero.feature.auth.usecase.LoginUseCase
import com.frame.zero.feature.auth.usecase.RegisterUseCase
import com.frame.zero.repository.auth.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val authModule: Module = module {
  single { AuthRepositoryImpl(get(), get(), get()) }
  single<AuthRepository> { get<AuthRepositoryImpl>() }
  single<SessionAuthOperations> { get<AuthRepositoryImpl>() }
  factory { LoginUseCase(get(), get()) }
  factory { RegisterUseCase(get(), get()) }
  factory { AuthViewModel(get(), get()) }
}
