package com.frame.zero.auth

import com.frame.zero.config.AppConfig
import com.frame.zero.repository.RefreshTokenRepository
import com.frame.zero.repository.RefreshTokenRepositoryExposed
import com.frame.zero.repository.UserRepository
import com.frame.zero.repository.UserRepositoryExposed
import org.koin.dsl.module

fun authModule(config: AppConfig) = module {
  single { config }
  single { config.jwt }
  single { PasswordHasher() }
  single { TokenHasher() }
  single { JwtService(get()) }
  single<UserRepository> { UserRepositoryExposed() }
  single<RefreshTokenRepository> { RefreshTokenRepositoryExposed() }
  single { AuthService(get(), get(), get(), get(), get(), get()) }
}
