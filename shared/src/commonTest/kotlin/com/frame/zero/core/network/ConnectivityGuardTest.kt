package com.frame.zero.core.network

import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.network.connectivity.OfflineException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConnectivityGuardTest {
  private class FixedConnectivity(
    private val online: Boolean
  ) : ConnectivityObserver {
    override val isOnline: Flow<Boolean> = flowOf(online)

    override fun isCurrentlyOnline(): Boolean = online
  }

  private fun clientWithGuard(online: Boolean): HttpClient {
    val engine = MockEngine { respond("ok", HttpStatusCode.OK) }
    return HttpClient(engine) {
      install(connectivityGuard(FixedConnectivity(online)))
    }
  }

  @Test
  fun `offline fails fast with an OfflineException before hitting the engine`() =
    runTest {
      val client = clientWithGuard(online = false)

      assertFailsWith<OfflineException> {
        client.get("https://example.com/api")
      }
    }

  @Test
  fun `online lets the request through`() =
    runTest {
      val client = clientWithGuard(online = true)

      val response = client.get("https://example.com/api")

      assertEquals("ok", response.bodyAsText())
    }
}
