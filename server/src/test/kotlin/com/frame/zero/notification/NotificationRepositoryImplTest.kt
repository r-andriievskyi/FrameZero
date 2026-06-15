package com.frame.zero.notification

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val notifications = NotificationRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newUser(email: String = "u@x.com"): UUID = users.create(email, "h", "Us", "Er").id

  @Test
  fun `create persists an unread notification`() =
    runBlocking {
      val userId = newUser()

      val record = notifications.create(userId, "Hello", "World")

      assertEquals("Hello", record.title)
      assertEquals("World", record.body)
      assertNull(record.readAt, "a new notification is unread")
      assertEquals(1, notifications.countUnread(userId))
    }

  @Test
  fun `countUnread counts only unread notifications for that user`() =
    runBlocking {
      val userId = newUser("u@x.com")
      val otherId = newUser("other@x.com")
      val read = notifications.create(userId, "Read", null)
      notifications.create(userId, "Unread", null)
      notifications.create(otherId, "Theirs", null)
      notifications.markRead(userId, listOf(read.id))

      assertEquals(1, notifications.countUnread(userId), "the other user's notification is not counted")
    }

  @Test
  fun `markRead marks only the given ids and leaves the rest unread`() =
    runBlocking {
      val userId = newUser()
      val target = notifications.create(userId, "Target", null)
      notifications.create(userId, "Keep", null)

      notifications.markRead(userId, listOf(target.id))

      assertEquals(1, notifications.countUnread(userId))
      val (items, _) = notifications.findForUser(userId, 20, null)
      assertTrue(items.first { it.id == target.id }.readAt != null, "target must be marked read")
    }

  @Test
  fun `markRead does not mark another user's notification`() =
    runBlocking {
      val owner = newUser("owner@x.com")
      val attacker = newUser("attacker@x.com")
      val ownersNotification = notifications.create(owner, "Private", null)

      // Attacker tries to mark a notification that belongs to someone else.
      notifications.markRead(attacker, listOf(ownersNotification.id))

      assertEquals(1, notifications.countUnread(owner), "the owner's notification must stay unread")
    }

  @Test
  fun `markAllRead clears every unread notification for the user only`() =
    runBlocking {
      val userId = newUser("u@x.com")
      val otherId = newUser("other@x.com")
      notifications.create(userId, "One", null)
      notifications.create(userId, "Two", null)
      notifications.create(otherId, "Theirs", null)

      notifications.markAllRead(userId)

      assertEquals(0, notifications.countUnread(userId))
      assertEquals(1, notifications.countUnread(otherId), "another user is unaffected")
    }

  @Test
  fun `findForUser returns only the user's notifications, newest first`() =
    runBlocking {
      val userId = newUser("u@x.com")
      val otherId = newUser("other@x.com")
      notifications.create(userId, "Mine 1", null)
      notifications.create(userId, "Mine 2", null)
      notifications.create(otherId, "Theirs", null)

      val (items, _) = notifications.findForUser(userId, 20, null)

      assertEquals(2, items.size, "scoped to the requesting user")
      assertTrue(items.all { it.userId == userId })
      val timestamps = items.map { it.createdAt.toEpochMilliseconds() }
      assertEquals(timestamps.sortedDescending(), timestamps, "ordered newest first")
    }

  @Test
  fun `findForUser paginates over every notification exactly once`() =
    runBlocking {
      val userId = newUser()
      val ids = (1..5).map { notifications.create(userId, "n$it", null).id }.toSet()

      val collected = mutableListOf<UUID>()
      var cursor: String? = null
      do {
        val (page, next) = notifications.findForUser(userId, 2, cursor)
        collected += page.map { it.id }
        cursor = next
      } while (cursor != null)

      assertEquals(ids, collected.toSet(), "every notification must appear")
      assertEquals(ids.size, collected.size, "no notification may be paged twice")
    }

  @Test
  fun `markRead with an empty id list is a no-op`() =
    runBlocking {
      val userId = newUser()
      notifications.create(userId, "Unread", null)

      notifications.markRead(userId, emptyList())

      assertEquals(1, notifications.countUnread(userId))
    }
}
