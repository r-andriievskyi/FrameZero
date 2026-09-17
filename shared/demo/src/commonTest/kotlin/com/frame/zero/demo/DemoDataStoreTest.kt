package com.frame.zero.demo

import com.frame.zero.domain.task.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DemoDataStoreTest {
  @Test
  fun seeds_productions_and_tasks() {
    val store = DemoDataStore()
    assertEquals(4, store.productions.value.size)
    assertTrue(store.tasks.value.isNotEmpty())
  }

  @Test
  fun add_task_prepends() {
    val store = DemoDataStore()
    val before = store.tasks.value.size
    val production = store.productions.value.first()
    store.addTask(
      store.tasks.value.first().copy(id = "new-task", productionId = production.id, status = TaskStatus.OPEN)
    )
    assertEquals(before + 1, store.tasks.value.size)
    assertEquals("new-task", store.tasks.value.first().id)
  }

  @Test
  fun complete_task_marks_done() {
    val store = DemoDataStore()
    val open = store.tasks.value.first { it.status == TaskStatus.OPEN }
    val updated = store.completeTask(open.id)
    assertEquals(TaskStatus.DONE, updated?.status)
    assertEquals(TaskStatus.DONE, store.getTask(open.id)?.status)
  }

  @Test
  fun reset_restores_pristine_seed() {
    val store = DemoDataStore()
    val seededTaskCount = store.tasks.value.size
    store.completeTask(store.tasks.value.first { it.status == TaskStatus.OPEN }.id)
    store.deleteProduction(store.productions.value.first().id)
    store.reset()
    assertEquals(4, store.productions.value.size)
    assertEquals(seededTaskCount, store.tasks.value.size)
    assertTrue(store.tasks.value.any { it.status == TaskStatus.OPEN })
  }
}
