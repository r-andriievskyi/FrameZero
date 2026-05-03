package com.frame.zero.domain

sealed interface DomainError {
  data object InvalidCredentials : DomainError

  data object EmailAlreadyExists : DomainError

  data class Network(val message: String) : DomainError

  data class Unknown(val message: String? = null) : DomainError
}

class DomainException(val error: DomainError) : Exception(error.toString())
