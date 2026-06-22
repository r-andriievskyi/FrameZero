package com.frame.zero.feature.auth.data

import com.frame.zero.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRepositoryImplTest {
  @Test
  fun `getMe issues a GET to the auth me endpoint`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) { ME_JSON }

      repo.getMe()

      val request = requests.single()
      assertEquals(HttpMethod.Get, request.method)
      assertEquals("/auth/me", request.url.encodedPath)
    }

  @Test
  fun `getMe deserializes the user body`() =
    runTest {
      val user = repository { ME_JSON }.getMe()

      assertEquals("u1", user.id)
      assertEquals("ada@example.com", user.email)
      assertEquals("Ada", user.firstName)
      assertEquals("Lovelace", user.lastName)
    }

  private fun repository(
    requests: MutableList<HttpRequestData> = mutableListOf(),
    body: () -> String
  ): UserRepositoryImpl {
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
    return UserRepositoryImpl(client, NetworkConfig(baseUrl = "http://test", isDebug = false))
  }

  private companion object {
    const val ME_JSON =
      """{"id":"u1","email":"ada@example.com","firstName":"Ada","lastName":"Lovelace"}"""
  }
}
