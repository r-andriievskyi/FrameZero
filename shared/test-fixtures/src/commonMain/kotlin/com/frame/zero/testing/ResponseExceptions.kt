package com.frame.zero.testing

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

/** Mints a real Ktor [ResponseException] carrying [status] via a one-shot mock request. */
suspend fun responseException(status: HttpStatusCode): ResponseException {
  val client = HttpClient(MockEngine { respond(content = "error", status = status) }) { expectSuccess = true }
  return try {
    client.get("https://example.test/")
    error("Expected a ResponseException for status $status")
  } catch (e: ResponseException) {
    e
  } finally {
    client.close()
  }
}
