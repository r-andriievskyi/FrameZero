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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

private const val MAX_POOL_SIZE = 10

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
}

// DB work runs on a dispatcher whose parallelism is capped to the connection-pool
// size, so blocking JDBC calls can't starve other coroutines and in-flight queries
// never outnumber the connections available to serve them.
@OptIn(ExperimentalCoroutinesApi::class)
private val dbDispatcher = Dispatchers.IO.limitedParallelism(MAX_POOL_SIZE)

/**
 * Runs [block] inside a transaction on the bounded DB dispatcher. When invoked
 * within an existing `dbQuery` the call joins the outer transaction instead of
 * opening its own, so a service method that wraps several repository calls in one
 * `dbQuery` commits or rolls back all of them atomically.
 */
suspend fun <T> dbQuery(block: suspend JdbcTransaction.() -> T): T =
  withContext(dbDispatcher) { suspendTransaction { block() } }

/** Readiness check: returns true only if a trivial query reaches the database. */
suspend fun pingDatabase(): Boolean = runCatching { dbQuery { exec("SELECT 1") } }.isSuccess
