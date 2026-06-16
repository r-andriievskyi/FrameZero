package com.frame.zero.feature.production.details.testing

import androidx.paging.PagingData
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskSummaryDto
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.tasks.TasksRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal fun productionDetailDto(
  id: String = "p1",
  title: String = "Pilot"
): ProductionDetailDto =
  ProductionDetailDto(
    id = id,
    title = title,
    genre = Genre.DRAMA,
    logline = null,
    phase = ProductionPhase.IDEA,
    progressPercent = 0,
    daysLeft = 0,
    startDate = LocalDate(2026, 4, 1),
    wrapDate = LocalDate(2026, 5, 1),
    budgetCents = null,
    membersCount = 0,
    keyCrew = emptyList(),
    pipeline = emptyList(),
    createdAt = Instant.fromEpochMilliseconds(0),
    updatedAt = Instant.fromEpochMilliseconds(0)
  )

internal class FakeProductionsRepository(
  private val detail: ProductionDetailDto = productionDetailDto(),
  private val getThrows: Throwable? = null,
  private val deleteThrows: Throwable? = null
) : ProductionsRepository {
  val getIds: MutableList<String> = mutableListOf()
  val deletedIds: MutableList<String> = mutableListOf()

  override fun observeProductions(): Flow<PagingData<Production>> = flowOf(PagingData.empty())

  override suspend fun getDetails(productionId: String): ProductionDetailDto {
    getIds += productionId
    getThrows?.let { throw it }
    return detail
  }

  override suspend fun listMembers(productionId: String): List<ProductionMemberDto> = emptyList()

  override suspend fun create(request: CreateProductionRequest): ProductionDetailDto = error("not used")

  override suspend fun delete(productionId: String) {
    deletedIds += productionId
    deleteThrows?.let { throw it }
  }
}

internal class FakeTasksRepository(
  private val tasks: List<TaskSummaryDto> = emptyList(),
  private val listThrows: Throwable? = null
) : TasksRepository {
  val listedProductionIds: MutableList<String> = mutableListOf()

  override suspend fun getTask(id: String): TaskDetailDto = error("not used")

  override suspend fun completeTask(id: String): TaskDetailDto = error("not used")

  override suspend fun createTask(request: CreateTaskRequest): TaskDetailDto = error("not used")

  override suspend fun listForProduction(productionId: String): List<TaskSummaryDto> {
    listedProductionIds += productionId
    listThrows?.let { throw it }
    return tasks
  }
}

/** Mints a real Ktor [ResponseException] carrying [status] via a one-shot mock request. */
internal suspend fun responseException(status: HttpStatusCode): ResponseException {
  val client = HttpClient(MockEngine { respond(content = "error", status = status) }) { expectSuccess = true }
  return try {
    client.get("https://example.test/")
    error("Expected a ResponseException for status $status")
  } catch (e: ResponseException) {
    e
  } finally {
    client.close()
  }
}
