package com.frame.zero.config

import com.frame.zero.database.RefreshTokensTable
import com.frame.zero.database.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
  fun init(config: DatabaseConfig): Database {
    val database = Database.connect(dataSource(config))
    transaction(database) { SchemaUtils.create(UsersTable, RefreshTokensTable) }
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
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
      }
    )

  private const val MAX_POOL_SIZE = 10
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
  newSuspendedTransaction(Dispatchers.IO) { block() }
