package com.frame.zero.domain

import kotlin.coroutines.cancellation.CancellationException

abstract class UseCase<in Params, out T> {
  protected open fun mapError(throwable: Throwable): DomainError = throwable.toDomainError()

  protected abstract suspend fun execute(params: Params): T

  suspend operator fun invoke(params: Params): Outcome<T> =
    runCatching { execute(params) }
      .fold(
        onSuccess = { Outcome.Success(it) },
        onFailure = {
          if (it is CancellationException) throw it
          Outcome.Failure(mapError(it))
        }
      )
}

abstract class NoParamsUseCase<out T> {
  protected open fun mapError(throwable: Throwable): DomainError = throwable.toDomainError()

  protected abstract suspend fun execute(): T

  suspend operator fun invoke(): Outcome<T> =
    runCatching { execute() }
      .fold(
        onSuccess = { Outcome.Success(it) },
        onFailure = {
          if (it is CancellationException) throw it
          Outcome.Failure(mapError(it))
        }
      )
}
