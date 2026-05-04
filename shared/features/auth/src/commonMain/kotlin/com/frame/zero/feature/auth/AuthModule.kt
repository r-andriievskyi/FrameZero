package com.frame.zero.feature.auth

import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.feature.auth.data.AuthRepositoryImpl
import com.frame.zero.feature.auth.data.UserRepositoryImpl
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInViewModel
import com.frame.zero.feature.auth.domain.LoginUseCase
import com.frame.zero.feature.auth.domain.RegisterUseCase
import com.frame.zero.repository.auth.AuthRepository
import com.frame.zero.repository.user.UserRepository
import org.koin.dsl.module

val authModule = module {
  single { AuthRepositoryImpl(get(), get(), get()) }
  single<AuthRepository> { get<AuthRepositoryImpl>() }
  single<SessionAuthOperations> { get<AuthRepositoryImpl>() }
  single<UserRepository> { UserRepositoryImpl(get(), get()) }
  factory { LoginUseCase(get(), get()) }
  factory { RegisterUseCase(get(), get()) }
  factory { SignInViewModel(get()) }
  factory { RegisterViewModel(get()) }
}
