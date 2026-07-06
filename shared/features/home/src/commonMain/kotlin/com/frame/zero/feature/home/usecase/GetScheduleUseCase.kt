package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.datetime.LocalDate

class GetScheduleUseCase(
  private val scheduleRepository: ScheduleRepository
) : UseCase<GetScheduleUseCase.Params, Schedule>() {
  data class Params(
    val view: ScheduleView,
    val date: LocalDate
  )

  override suspend fun execute(params: Params): Schedule = scheduleRepository.getSchedule(params.view, params.date)
}
