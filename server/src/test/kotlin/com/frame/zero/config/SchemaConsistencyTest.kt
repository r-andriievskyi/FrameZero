package com.frame.zero.config

import com.frame.zero.auth.RefreshTokensTable
import com.frame.zero.auth.UsersTable
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.notification.DeviceTokensTable
import com.frame.zero.notification.NotificationsTable
import com.frame.zero.production.ProductionMembersTable
import com.frame.zero.production.ProductionsTable
import com.frame.zero.schedule.ScheduleEventsTable
import com.frame.zero.task.TasksTable
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards the hand-maintained contract between the Flyway migrations (the
 * schema's source of truth) and the Exposed `Table` definitions the code
 * queries through. If either side changes without the other, this fails and
 * prints the DDL Exposed considers missing.
 */
class SchemaConsistencyTest {
  private val db = PostgresTestDatabase()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  @Test
  fun `exposed table definitions match the flyway-migrated schema`() {
    val missingDdl =
      transaction {
        SchemaUtils.statementsRequiredToActualizeScheme(
          UsersTable,
          RefreshTokensTable,
          ProductionsTable,
          ProductionMembersTable,
          TasksTable,
          ScheduleEventsTable,
          NotificationsTable,
          DeviceTokensTable
        )
      }
    assertTrue(
      missingDdl.isEmpty(),
      "Exposed Table definitions drifted from the Flyway-migrated schema. " +
        "Update the Table defs or add a migration:\n" + missingDdl.joinToString("\n")
    )
  }
}
