package com.frame.zero.config

import com.frame.zero.auth.RefreshTokensTable
import com.frame.zero.auth.UsersTable
import com.frame.zero.notification.NotificationsTable
import com.frame.zero.production.ProductionMembersTable
import com.frame.zero.production.ProductionsTable
import com.frame.zero.schedule.ScheduleEventsTable
import com.frame.zero.task.TasksTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {
  fun init(config: DatabaseConfig): Database {
    val ds = dataSource(config)
    val database = Database.connect(ds)
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
    return database
  }

  private fun dataSource(config: DatabaseConfig): DataSource =
    HikariDataSource(
      HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = config.url
        username = config.user
        password = config.password
        maximumPoolSize = MAX_POOL_SIZE
        // Exposed manages transactions explicitly via suspendTransaction
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED"
        validate()
      }
    )

  private const val MAX_POOL_SIZE = 10
}

suspend fun <T> dbQuery(block: suspend () -> T): T = suspendTransaction { block() }
