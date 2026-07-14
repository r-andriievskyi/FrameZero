package com.frame.zero.demo

import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
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
import kotlin.time.Duration.Companion.minutes

/**
 * Single in-memory source of truth for demo builds. Every `Demo*Repository` reads and mutates
 * this store, so a task created on one screen shows up everywhere the same way the real
 * offline-first repositories would. [reset] restores the pristine seed on sign-out.
 */
internal class DemoDataStore {
  private val _productions = MutableStateFlow<List<ProductionDetail>>(emptyList())
  val productions: StateFlow<List<ProductionDetail>> = _productions.asStateFlow()

  private val _tasks = MutableStateFlow<List<TaskDetail>>(emptyList())
  val tasks: StateFlow<List<TaskDetail>> = _tasks.asStateFlow()

  private val _conversations = MutableStateFlow<Map<String, Conversation>>(emptyMap())
  val conversations: StateFlow<Map<String, Conversation>> = _conversations.asStateFlow()

  private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
  val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

  init {
    reset()
  }

  fun reset() {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val productions = DemoData.seedProductions(now, today)
    _productions.update { productions }
    _tasks.update { DemoData.seedTasks(now, today, productions) }
    _conversations.update { emptyMap() }
    _messages.update { emptyMap() }
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

  fun conversationFor(taskId: String): Conversation? = _conversations.value[taskId]

  fun getOrCreateConversation(taskId: String): Conversation {
    _conversations.value[taskId]?.let { return it }
    val now = Clock.System.now()
    val conversationId = "conv-$taskId"
    val task = getTask(taskId)
    val crew = task?.let { getProduction(it.productionId)?.keyCrew }
      ?.filter { it.userId != null && it.userId != DemoData.USER_ID }
      .orEmpty()
    val seedLines = listOf(
      "Kicking off the thread for this one.",
      "I'll have the first pass ready tomorrow.",
      "Great — flag me if anything blocks you.",
      "Location's confirmed, sending the address now."
    )
    val seeded = seedLines.mapIndexed { index, body ->
      val sender = if (crew.isEmpty()) DemoData.USER_ID else crew[index % crew.size].userId!!
      ChatMessage(
        id = "$conversationId-m$index",
        conversationId = conversationId,
        ordinal = (index + 1).toLong(),
        senderUserId = sender,
        body = body,
        clientMessageId = "$conversationId-seed$index",
        createdAt = now - (seedLines.size - index).minutes
      )
    }
    val conversation = Conversation(
      id = conversationId,
      taskId = taskId,
      productionId = task?.productionId.orEmpty(),
      createdAt = now - 10.minutes,
      latestOrdinal = seeded.size.toLong(),
      lastReadOrdinal = (seeded.size - 1).toLong()
    )
    _messages.update { it + (conversationId to seeded) }
    _conversations.update { it + (taskId to conversation) }
    return conversation
  }

  fun appendMessage(
    conversationId: String,
    senderUserId: String,
    body: String,
    clientMessageId: String
  ) {
    val now = Clock.System.now()
    var ordinal = 0L
    _messages.update { byConversation ->
      val existing = byConversation[conversationId].orEmpty()
      ordinal = (existing.maxOfOrNull { it.ordinal } ?: 0L) + 1
      val message = ChatMessage(
        id = "$conversationId-m$ordinal",
        conversationId = conversationId,
        ordinal = ordinal,
        senderUserId = senderUserId,
        body = body,
        clientMessageId = clientMessageId,
        createdAt = now
      )
      byConversation + (conversationId to (existing + message))
    }
    _conversations.update { map ->
      val taskEntry = map.entries.firstOrNull { it.value.id == conversationId } ?: return@update map
      val bumped = taskEntry.value.copy(
        // maxOf: two appends may commit their conversation bumps out of ordinal order.
        latestOrdinal = maxOf(taskEntry.value.latestOrdinal, ordinal),
        // The sender's own message is implicitly read; keep the cursor at the sender's message.
        lastReadOrdinal = if (senderUserId == DemoData.USER_ID) {
          maxOf(taskEntry.value.lastReadOrdinal, ordinal)
        } else {
          taskEntry.value.lastReadOrdinal
        }
      )
      map + (taskEntry.key to bumped)
    }
  }

  /** A crew member to voice the canned reply after the demo user sends a message. */
  fun replySenderFor(conversationId: String): String? {
    val taskId = _conversations.value.entries.firstOrNull { it.value.id == conversationId }?.value?.taskId
    val productionId = taskId?.let { getTask(it)?.productionId }
    return productionId
      ?.let { getProduction(it)?.keyCrew }
      ?.firstOrNull { it.userId != null && it.userId != DemoData.USER_ID }
      ?.userId
  }

  fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ) {
    _conversations.update { map ->
      val entry = map.entries.firstOrNull { it.value.id == conversationId } ?: return@update map
      val clamped = lastReadOrdinal.coerceIn(0, entry.value.latestOrdinal)
      map + (entry.key to entry.value.copy(lastReadOrdinal = clamped))
    }
  }
}
