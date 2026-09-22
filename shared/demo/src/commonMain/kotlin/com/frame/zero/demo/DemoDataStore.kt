package com.frame.zero.demo

import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.task.TaskDetail
import com.frame.zero.domain.task.TaskParticipant
import com.frame.zero.domain.task.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Single in-memory source of truth for demo builds. Every `Demo*Repository` reads and mutates
 * this store, so a task created on one screen shows up everywhere the same way the real
 * offline-first repositories would. [reset] restores the pristine seed on sign-out.
 */
class DemoDataStore {
  private val _productions = MutableStateFlow<List<ProductionDetail>>(emptyList())
  val productions: StateFlow<List<ProductionDetail>> = _productions.asStateFlow()

  private val _tasks = MutableStateFlow<List<TaskDetail>>(emptyList())
  val tasks: StateFlow<List<TaskDetail>> = _tasks.asStateFlow()

  init {
    reset()
  }

  fun reset() {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val productions = DemoData.seedProductions(now, today)
    _productions.update { productions }
    _tasks.update { DemoData.seedTasks(now, today, productions) }
  }

  fun getProduction(id: String): ProductionDetail? = _productions.value.firstOrNull { it.id == id }

  fun addProduction(detail: ProductionDetail) {
    _productions.update { listOf(detail) + it }
  }

  fun deleteProduction(id: String) {
    _productions.update { list -> list.filterNot { it.id == id } }
    _tasks.update { list -> list.filterNot { it.productionId == id } }
  }

  fun getTask(id: String): TaskDetail? = _tasks.value.firstOrNull { it.id == id }

  fun tasksForProduction(productionId: String): List<TaskDetail> =
    _tasks.value.filter { it.productionId == productionId }

  fun addTask(detail: TaskDetail) {
    _tasks.update { listOf(detail) + it }
  }

  fun completeTask(id: String): TaskDetail? {
    var updated: TaskDetail? = null
    _tasks.update { list ->
      list.map { task ->
        if (task.id == id) task.copy(status = TaskStatus.DONE).also { updated = it } else task
      }
    }
    return updated
  }

  fun updateParticipants(
    taskId: String,
    userIds: List<String>
  ): TaskDetail? {
    val participants = userIds.map { userId ->
      val member = memberFor(userId)
      TaskParticipant(
        userId = userId,
        name = member?.name ?: userId,
        avatarColorHex = member?.avatarColorHex
      )
    }
    var updated: TaskDetail? = null
    _tasks.update { list ->
      list.map { task ->
        if (task.id == taskId) task.copy(participants = participants).also { updated = it } else task
      }
    }
    return updated
  }

  private fun memberFor(userId: String): ProductionMember? =
    _productions.value.asSequence()
      .flatMap { it.keyCrew.asSequence() }
      .firstOrNull { it.userId == userId }
}
