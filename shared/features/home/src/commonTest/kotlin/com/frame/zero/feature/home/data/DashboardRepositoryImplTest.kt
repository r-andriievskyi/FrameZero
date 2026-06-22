package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardRepositoryImplTest {
  @Test
  fun `getDashboard requests the dashboard endpoint`() = runTest {
    val requests = mutableListOf<HttpRequestData>()
    val repo = repository(requests) { DASHBOARD_JSON }

    repo.getDashboard()

    assertEquals("/api/v1/dashboard", requests.single().url.encodedPath)
  }

  @Test
  fun `getDashboard deserializes the response body`() = runTest {
    val repo = repository { DASHBOARD_JSON }

    val response = repo.getDashboard()

    assertEquals("Ada", response.greeting.displayName)
    assertEquals(2, response.stats.activeProjects)
    assertEquals(3, response.stats.openTasks)
  }

  private fun repository(
    requests: MutableList<HttpRequestData> = mutableListOf(), body: () -> String
  ): DashboardRepositoryImpl {
    val client = HttpClient(
      MockEngine { request ->
        requests += request
        respond(
          content = body(), headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      }) {
      install(ContentNegotiation) { json() }
    }
    return DashboardRepositoryImpl(client, NetworkConfig(baseUrl = "http://test", isDebug = false))
  }

  private companion object {
    val DASHBOARD_JSON = """
      {
        "greeting":{"displayName":"Ada","activeProductionsCount":2,"openTasksCount":3},
        "stats":{"activeProjects":2,"openTasks":3},
        "myTasks":[]
      }
      """.trimIndent()
  }
}
