package com.frame.zero.common

import com.frame.zero.config.dbQuery

/**
 * Service-method transaction boundary. A service wraps its body in
 * [transaction] so every repository call it makes commits or rolls back as a
 * unit — a mid-method failure can't leave partial writes.
 *
 * Injected (rather than calling [dbQuery] directly) so tests backed by in-memory
 * fakes can supply a pass-through implementation that needs no database.
 */
interface Transactor {
  suspend fun <T> transaction(block: suspend () -> T): T
}

class ExposedTransactor : Transactor {
  override suspend fun <T> transaction(block: suspend () -> T): T = dbQuery { block() }
}
