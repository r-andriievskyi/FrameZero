package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleView
import com.frame.zero.domain.schedule.toDomain
import com.frame.zero.repository.schedule.ScheduleRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

class GetScheduleUseCase(
  private val scheduleRepository: ScheduleRepository
) : UseCase<GetScheduleUseCase.Params, Schedule>() {
  data class Params(
    val view: ScheduleView,
    val date: LocalDate
  )

  override suspend fun execute(params: Params): Schedule {
    val dateParam =
      when (params.view) {
        ScheduleView.MONTH ->
          "${params.date.year}-${params.date.month.number.toString().padStart(2, '0')}"
        else -> params.date.toString()
      }
    return scheduleRepository.getSchedule(params.view.name.lowercase(), dateParam).toDomain()
  }
}
