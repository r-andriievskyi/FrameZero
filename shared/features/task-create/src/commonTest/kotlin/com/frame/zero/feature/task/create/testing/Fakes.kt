package com.frame.zero.feature.task.create.testing

import androidx.paging.PagingData
import com.frame.zero.domain.production.Production
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.repository.productions.ProductionsRepository
import com.frame.zero.repository.tasks.TasksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

internal fun productionMemberDto(
  id: String = "m1",
  userId: String? = "u1",
  name: String = "Ada",
  role: String = "Director",
  initials: String = "AD",
  avatarColorHex: String? = "#FF0000"
): ProductionMemberDto =
  ProductionMemberDto(
    id = id,
    userId = userId,
    name = name,
    role = role,
    initials = initials,
    avatarColorHex = avatarColorHex,
    addedAt = Instant.fromEpochMilliseconds(0)
  )

internal fun taskDetailDto(
  id: String = "t1",
  productionId: String = "p1",
  title: String = "Storyboard",
  description: String? = null,
  priority: TaskPriority = TaskPriority.MEDIUM
): TaskDetailDto =
  TaskDetailDto(
    id = id,
    productionId = productionId,
    productionTitle = "Pilot",
    title = title,
    description = description,
    dueDate = null,
    status = TaskStatus.OPEN,
    priority = priority,
    assigneeUserId = null,
    assignee = null,
    createdAt = Instant.fromEpochMilliseconds(0)
  )

internal class FakeTasksRepository(
  private val created: TaskDetailDto = taskDetailDto(),
  private val createThrows: Throwable? = null
) : TasksRepository {
  val createRequests: MutableList<CreateTaskRequest> = mutableListOf()

  override suspend fun getTask(id: String): TaskDetailDto = error("not used")

  override suspend fun completeTask(id: String): TaskDetailDto = error("not used")

  override suspend fun createTask(request: CreateTaskRequest): TaskDetailDto {
    createRequests += request
    createThrows?.let { throw it }
    return created
  }

  override suspend fun listForProduction(productionId: String) = error("not used")
}

internal class FakeProductionsRepository(
  private val members: List<ProductionMemberDto> = emptyList(),
  private val listMembersThrows: Throwable? = null
) : ProductionsRepository {
  val listMembersCalls: MutableList<String> = mutableListOf()

  override fun observeProductions(): Flow<PagingData<Production>> = flowOf(PagingData.empty())

  override suspend fun getDetails(productionId: String): ProductionDetailDto = error("not used")

  override suspend fun listMembers(productionId: String): List<ProductionMemberDto> {
    listMembersCalls += productionId
    listMembersThrows?.let { throw it }
    return members
  }

  override suspend fun create(request: CreateProductionRequest): ProductionDetailDto = error("not used")

  override suspend fun delete(productionId: String): Unit = error("not used")
}
