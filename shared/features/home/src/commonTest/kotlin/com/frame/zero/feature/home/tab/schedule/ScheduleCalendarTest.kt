package com.frame.zero.feature.home.tab.schedule

import com.frame.zero.domain.schedule.Schedule
import com.frame.zero.domain.schedule.ScheduleDay
import com.frame.zero.domain.schedule.ScheduleTask
import com.frame.zero.domain.task.TaskPriority
import com.frame.zero.domain.task.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

class ScheduleCalendarTest {
  @Test
  fun weekStartIsMondayOfTheSameWeek() {
    assertEquals(LocalDate(2026, 4, 20), weekStartFor(LocalDate(2026, 4, 26))) // Sunday
    assertEquals(LocalDate(2026, 4, 20), weekStartFor(LocalDate(2026, 4, 20))) // Monday
  }

  @Test
  fun weekOfListsSevenDaysFromMonday() {
    val week = weekOf(LocalDate(2026, 4, 30))

    assertEquals(7, week.size)
    assertEquals(LocalDate(2026, 4, 27), week.first())
    assertEquals(LocalDate(2026, 5, 3), week.last())
  }

  @Test
  fun monthGridPadsToMondayAndListsEveryDay() {
    // April 2026 starts on a Wednesday.
    val april = monthGrid(2026, Month.APRIL)
    assertEquals(2, april.leadingBlanks)
    assertEquals(30, april.days.size)
    assertEquals(LocalDate(2026, 4, 30), april.days.last())

    // June 2026 starts on a Monday; February 2028 is a leap month.
    assertEquals(0, monthGrid(2026, Month.JUNE).leadingBlanks)
    assertEquals(29, monthGrid(2028, Month.FEBRUARY).days.size)
  }

  @Test
  fun daysWithItemsSkipsEmptyDays() {
    val busy = LocalDate(2026, 4, 22)
    val schedule = Schedule(
      rangeStart = LocalDate(2026, 4, 20),
      rangeEnd = LocalDate(2026, 4, 26),
      days = listOf(
        ScheduleDay(date = LocalDate(2026, 4, 21), events = emptyList(), tasks = emptyList()),
        ScheduleDay(date = busy, events = emptyList(), tasks = listOf(scheduleTask(busy)))
      )
    )

    assertEquals(setOf(busy), schedule.daysWithItems())
    assertEquals(emptySet(), (null as Schedule?).daysWithItems())
  }

  private fun scheduleTask(dueDate: LocalDate) =
    ScheduleTask(
      id = "t1",
      title = "Task",
      productionId = "p1",
      productionTitle = "Film",
      dueDate = dueDate,
      status = TaskStatus.OPEN,
      priority = TaskPriority.HIGH
    )
}
