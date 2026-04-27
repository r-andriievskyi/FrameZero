package com.frame.zero.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {}

fun androidContextModule(context: Context): Module = module { single<Context> { context } }
