package com.frame.zero.di

import com.frame.zero.feature.auth.AuthViewModel
import org.koin.dsl.module

val sharedModule = module {
  factory { AuthViewModel() }
}
