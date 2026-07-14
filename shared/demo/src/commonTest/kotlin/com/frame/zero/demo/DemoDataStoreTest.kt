package com.frame.zero.demo

import com.frame.zero.domain.task.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
  fun conversation_is_seeded_with_monotonic_ordinals() {
    val store = DemoDataStore()
    val taskId = store.tasks.value.first().id
    val conversation = store.getOrCreateConversation(taskId)
    val messages = store.messages.value.getValue(conversation.id)
    assertTrue(messages.isNotEmpty())
    assertEquals(messages.map { it.ordinal }.sorted(), messages.map { it.ordinal })
    assertEquals(messages.maxOf { it.ordinal }, conversation.latestOrdinal)
    // getOrCreate is idempotent.
    assertEquals(conversation.id, store.getOrCreateConversation(taskId).id)
  }

  @Test
  fun append_message_bumps_latest_ordinal() {
    val store = DemoDataStore()
    val taskId = store.tasks.value.first().id
    val conversation = store.getOrCreateConversation(taskId)
    val before = conversation.latestOrdinal
    store.appendMessage(conversation.id, DemoData.USER_ID, "hello", "client-1")
    val after = store.conversationFor(taskId)
    assertNotNull(after)
    assertEquals(before + 1, after.latestOrdinal)
    // Sender's own message keeps the read cursor current.
    assertEquals(after.latestOrdinal, after.lastReadOrdinal)
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
