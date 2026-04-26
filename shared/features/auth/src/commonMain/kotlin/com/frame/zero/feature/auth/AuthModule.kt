package com.frame.zero.feature.auth

import org.koin.dsl.module

val featureAuthModule = module { factory { AuthViewModel(authRepository = get()) } }
