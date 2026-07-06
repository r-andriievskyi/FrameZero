package com.frame.zero.feature.gallery

import com.arkivanov.decompose.ComponentContext

class DesignSystemGalleryComponent(
  componentContext: ComponentContext,
  val onBack: () -> Unit
) : ComponentContext by componentContext
