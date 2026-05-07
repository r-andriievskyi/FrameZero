package com.frame.zero.common.testing

import com.frame.zero.auth.RefreshTokensTable
import com.frame.zero.auth.UsersTable
import com.frame.zero.notification.NotificationsTable
import com.frame.zero.production.ProductionMembersTable
import com.frame.zero.production.ProductionsTable
import com.frame.zero.schedule.ScheduleEventsTable
import com.frame.zero.task.TasksTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

/**
 * Spins up a uniquely-named H2 in-memory database with the production schema for the duration of a
 * single test, then drops it. Each test gets a fresh database, so tests don't share state.
 */
class H2TestDatabase {
  private lateinit var database: Database

  fun start() {
    val name = UUID.randomUUID().toString()
    database =
      Database.connect(
        url = "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE",
        driver = "org.h2.Driver"
      )
    transaction(database) {
      SchemaUtils.create(
        UsersTable,
        RefreshTokensTable,
        ProductionsTable,
        ProductionMembersTable,
        TasksTable,
        ScheduleEventsTable,
        NotificationsTable
      )
    }
  }

  fun stop() {
    transaction(database) {
      SchemaUtils.drop(
        NotificationsTable,
        ScheduleEventsTable,
        TasksTable,
        ProductionMembersTable,
        ProductionsTable,
        RefreshTokensTable,
        UsersTable
      )
    }
    TransactionManager.closeAndUnregister(database)
  }
}
