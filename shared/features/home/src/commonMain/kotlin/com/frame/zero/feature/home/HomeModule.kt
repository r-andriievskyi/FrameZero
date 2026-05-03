package com.frame.zero.feature.home

import org.koin.core.module.Module
import org.koin.dsl.module

// Empty for now — tab ViewModels have no dependencies yet so they're constructed directly inside
// their components. When use cases / repositories are wired in, register e.g.
// `factory { DashboardTabViewModel(get()) }` here and pass factories through HomeComponent's
// ctor (mirroring AuthComponent).
val featureHomeModule: Module = module {}
