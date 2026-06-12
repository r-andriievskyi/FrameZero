package com.frame.zero.common.testing

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

// One Postgres container shared across the whole test JVM (container startup is the
// expensive part) — migrated once via the real Flyway scripts, so the repository
// suite runs against the same schema and dialect production uses, not an H2
// approximation. Per-test isolation comes from TRUNCATE in start().
private val sharedPostgres: PostgreSQLContainer<*> by lazy {
  PostgreSQLContainer("postgres:16-alpine").apply {
    start()
    Flyway.configure().dataSource(jdbcUrl, username, password).load().migrate()
  }
}

// Derived from the migrated schema so tables added by future Flyway migrations
// are reset automatically instead of silently leaking rows across tests.
private val tablesToTruncate: String by lazy {
  sharedPostgres.createConnection("").use { connection ->
    connection.createStatement().use { statement ->
      val rs = statement.executeQuery(
        "SELECT tablename FROM pg_tables " +
          "WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'"
      )
      buildList { while (rs.next()) add(rs.getString(1)) }.joinToString(", ")
    }
  }
}

class PostgresTestDatabase {
  private lateinit var database: Database

  fun start() {
    val container = sharedPostgres
    database =
      Database.connect(
        url = container.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = container.username,
        password = container.password
      )
    transaction(database) {
      exec("TRUNCATE TABLE $tablesToTruncate RESTART IDENTITY CASCADE")
    }
  }

  fun stop() {
    TransactionManager.closeAndUnregister(database)
  }
}
