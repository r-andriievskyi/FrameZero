package com.frame.zero.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import javax.sql.DataSource

object DatabaseFactory {
  fun init(config: DatabaseConfig): Database {
    val ds = dataSource(config)
    runFlyway(ds)
    return Database.connect(ds)
  }

  private fun runFlyway(ds: DataSource) {
    val isDev = System.getProperty("io.ktor.development")?.toBoolean() == true
    Flyway
      .configure()
      .dataSource(ds)
      .locations("classpath:db/migration")
      .baselineOnMigrate(isDev)
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
        // Exposed manages transactions explicitly via suspendTransaction
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED"
        validate()
      }
    )

  private const val MAX_POOL_SIZE = 10
}

suspend fun <T> dbQuery(block: suspend () -> T): T = suspendTransaction { block() }
