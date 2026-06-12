package com.frame.zero.auth

import com.frame.zero.common.ExposedTransactor
import com.frame.zero.common.Transactor
import com.frame.zero.config.AppConfig
import org.koin.dsl.module

fun authModule(config: AppConfig) =
  module {
    single { config }
    single { config.jwt }
    single<Transactor> { ExposedTransactor() }
    single { PasswordHasher() }
    single { TokenHasher() }
    single { JwtService(get()) }
    single<UserRepository> { UserRepositoryImpl() }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl() }
    single {
      AuthService(get(), get(), get(), get(), get(), get(), get())
    }
  }
