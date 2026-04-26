package com.frame.zero.domain

interface UseCase<in Params, out Result> {
  suspend operator fun invoke(params: Params): Result
}

interface NoParamsUseCase<out Result> {
  suspend operator fun invoke(): Result
}
