package com.frame.zero.domain

sealed interface Outcome<out T> {
  data class Success<T>(val data: T) : Outcome<T>

  data class Failure(val error: DomainError) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> =
  when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
  }

inline fun <T> Outcome<T>.onSuccess(action: (T) -> Unit): Outcome<T> = also {
  if (this is Outcome.Success) action(data)
}

inline fun <T> Outcome<T>.onFailure(action: (DomainError) -> Unit): Outcome<T> = also {
  if (this is Outcome.Failure) action(error)
}
