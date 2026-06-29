package com.frame.zero.feature.production

sealed interface CreateProductionEvent {
  /** The production was created; navigate away from the wizard. */
  data object Created : CreateProductionEvent
}
