package com.frame.zero.common.testing

import com.frame.zero.common.Transactor

/**
 * Pass-through [Transactor] for tests backed by in-memory fakes — there is no
 * database to open a transaction against, so the block just runs inline.
 */
class NoopTransactor : Transactor {
  override suspend fun <T> transaction(block: suspend () -> T): T = block()
}
