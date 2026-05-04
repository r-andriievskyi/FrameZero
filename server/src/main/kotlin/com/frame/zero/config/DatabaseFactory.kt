package com.frame.zero.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

object DatabaseFactory {
  fun init(config: DatabaseConfig): Database {
    val ds = dataSource(config)
    runFlyway(ds)
    return Database.connect(ds)
  }

  private fun runFlyway(ds: DataSource) {
    Flyway.configure()
      .dataSource(ds)
      .locations("classpath:db/migration")
      .baselineOnMigrate(true)
      .baselineVersion("0")
      .load()
      .migrate()
  }

  private fun dataSource(config: DatabaseConfig): DataSource =
    HikariDataSource(
      HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = config.url
        username = config.user
        password = config.password
        maximumPoolSize = MAX_POOL_SIZE
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
      }
    )

  private const val MAX_POOL_SIZE = 10
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
  withContext(Dispatchers.IO) { suspendTransaction { block() } }
