package com.frame.zero.feature.home.testing

import androidx.paging.PagingData
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.dto.dashboard.GreetingDto
import com.frame.zero.dto.dashboard.StatsDto
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.repository.dashboard.DashboardRepository
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.schedule.ScheduleRepository
import com.frame.zero.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

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

internal class FakeProductionsRepository(
  private val emissions: Flow<PagingData<Production>> = flowOf(PagingData.empty())
) : ProductionsRepository {
  val observeCalls: MutableList<ProductionPhase?> = mutableListOf()

  override fun observeProductions(phase: ProductionPhase?): Flow<PagingData<Production>> {
    observeCalls += phase
    return emissions
  }

  override suspend fun getDetails(productionId: String): ProductionDetailDto = error("not used")

  override suspend fun create(request: CreateProductionRequest): ProductionDetailDto = error("not used")

  override suspend fun delete(productionId: String): Unit = error("not used")
}

internal class FakeScheduleRepository(
  private val response: ScheduleResponse =
    ScheduleResponse(
      rangeStart = LocalDate(2026, 1, 1),
      rangeEnd = LocalDate(2026, 1, 1),
      days = emptyList()
    ),
  private val throws: Throwable? = null
) : ScheduleRepository {
  data class Call(
    val view: String,
    val date: String
  )

  val calls: MutableList<Call> = mutableListOf()

  override suspend fun getSchedule(
    view: String,
    date: String
  ): ScheduleResponse {
    calls += Call(view, date)
    throws?.let { throw it }
    return response
  }
}
