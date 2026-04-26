package com.frame.zero.repository.auth

import org.koin.dsl.module

val repositoryAuthModule = module { single<AuthRepository> { InMemoryAuthRepository() } }
