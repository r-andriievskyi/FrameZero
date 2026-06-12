package com.frame.zero.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import javax.sql.DataSource

private const val MAX_POOL_SIZE = 10

object DatabaseFactory {
  fun init(
    config: DatabaseConfig,
    meterRegistry: MeterRegistry? = null
  ): Database {
    val ds = dataSource(config, meterRegistry)
    // Flyway owns DDL via versioned SQL migrations (src/main/resources/db/migration).
    // The Exposed tables remain the typed query surface only.
    Flyway.configure().dataSource(ds).load().migrate()
    return Database.connect(ds)
  }

  private fun dataSource(
    config: DatabaseConfig,
    meterRegistry: MeterRegistry?
  ): DataSource =
    HikariDataSource(
      HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = config.url
        username = config.user
        password = config.password
        maximumPoolSize = MAX_POOL_SIZE
        // exposed manages transactions explicitly via suspendTransaction
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED"
        // Publish pool stats (active/idle/pending connections, acquire timing) to
        // Prometheus when a registry is supplied.
        if (meterRegistry != null) {
          metricsTrackerFactory = MicrometerMetricsTrackerFactory(meterRegistry)
        }
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

suspend fun pingDatabase(): Boolean = runCatching { dbQuery { exec("SELECT 1") } }.isSuccess
