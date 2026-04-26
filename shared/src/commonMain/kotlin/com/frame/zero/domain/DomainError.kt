package com.frame.zero.domain

sealed interface DomainError {
  data class Network(val message: String) : DomainError

  data class Unexpected(val cause: Throwable? = null) : DomainError
}
