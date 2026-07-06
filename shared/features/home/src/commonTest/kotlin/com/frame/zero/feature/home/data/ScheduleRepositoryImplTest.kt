package com.frame.zero.feature.home.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.schedule.ScheduleView
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
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleRepositoryImplTest {
  @Test
  fun `getSchedule requests the schedule endpoint with view and date query params`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) {
        """{"rangeStart":"2026-03-01","rangeEnd":"2026-03-31","days":[]}"""
      }

      repo.getSchedule(view = ScheduleView.MONTH, date = LocalDate(2026, 3, 5))

      val request = requests.single()
      assertEquals("/api/v1/schedule", request.url.encodedPath)
      assertEquals("month", request.url.parameters["view"])
      assertEquals("2026-03", request.url.parameters["date"])
    }

  @Test
  fun `getSchedule deserializes the response body`() =
    runTest {
      val repo = repository {
        """
        {
          "rangeStart":"2026-03-01",
          "rangeEnd":"2026-03-31",
          "days":[{"date":"2026-03-05","events":[],"tasks":[]}]
        }
        """.trimIndent()
      }

      val response = repo.getSchedule(view = ScheduleView.WEEK, date = LocalDate(2026, 3, 5))

      assertEquals(1, response.days.size)
      assertEquals("2026-03-01", response.rangeStart.toString())
    }

  private fun repository(
    requests: MutableList<HttpRequestData> = mutableListOf(),
    body: () -> String
  ): ScheduleRepositoryImpl {
    val client = HttpClient(
      MockEngine { request ->
        requests += request
        respond(
          content = body(),
          headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      }
    ) {
      install(ContentNegotiation) { json() }
    }
    return ScheduleRepositoryImpl(client, NetworkConfig(baseUrl = "http://test", isDebug = false))
  }
}
