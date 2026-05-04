package com.frame.zero.domain

abstract class UseCase<in Params, out T> {
  protected abstract fun mapError(throwable: Throwable): DomainError

  protected abstract suspend fun execute(params: Params): T

  suspend operator fun invoke(params: Params): Outcome<T> =
    runCatching { execute(params) }
      .fold(onSuccess = { Outcome.Success(it) }, onFailure = { Outcome.Failure(mapError(it)) })
}

abstract class NoParamsUseCase<out T> {
  protected abstract fun mapError(throwable: Throwable): DomainError

  protected abstract suspend fun execute(): T

  suspend operator fun invoke(): Outcome<T> =
    runCatching { execute() }
      .fold(onSuccess = { Outcome.Success(it) }, onFailure = { Outcome.Failure(mapError(it)) })
}
