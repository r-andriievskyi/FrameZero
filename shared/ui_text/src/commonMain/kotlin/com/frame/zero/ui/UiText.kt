package com.frame.zero.ui

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource

@Immutable
sealed interface UiText {
  data class Dynamic(
    val text: String
  ) : UiText

  data class Resource(
    val res: StringResource,
    val args: List<Any> = emptyList()
  ) : UiText
}

fun StringResource.asUiText(vararg args: Any): UiText = UiText.Resource(this, args.toList())
