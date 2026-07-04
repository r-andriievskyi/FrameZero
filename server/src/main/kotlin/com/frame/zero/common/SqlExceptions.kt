package com.frame.zero.common

import java.sql.SQLException

private const val UNIQUE_VIOLATION_SQL_STATE = "23505"

fun SQLException.isUniqueViolation(): Boolean =
  generateSequence(this as Throwable) { it.cause }
    .filterIsInstance<SQLException>()
    .any { it.sqlState == UNIQUE_VIOLATION_SQL_STATE }
