package com.frame.zero.feature.home.testing

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.user.UserRepository

internal class FakeUserRepository(
  private val userDto: UserDto = UserDto(id = "", email = "", firstName = "", lastName = ""),
  private val throws: Throwable? = null
) : UserRepository {
  var getMeCalls: Int = 0
    private set

  override suspend fun getMe(): UserDto {
    getMeCalls++
    throws?.let { throw it }
    return userDto
  }
}

internal class FakeDashboardRepository(
  private val response: DashboardResponse =
    DashboardResponse(
      greeting = GreetingDto(displayName = "", activeProductionsCount = 0, openTasksCount = 0),
      stats = StatsDto(activeProjects = 0, openTasks = 0),
      myTasks = emptyList()
    ),
  private val throws: Throwable? = null
) : DashboardRepository {
  var getDashboardCalls: Int = 0
    private set

  override suspend fun getDashboard(): DashboardResponse {
    getDashboardCalls++
    throws?.let { throw it }
    return response
  }
}
