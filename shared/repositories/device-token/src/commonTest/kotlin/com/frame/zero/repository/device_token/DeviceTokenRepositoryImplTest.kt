package com.frame.zero.repository.device_token

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.dto.device.DevicePlatform
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceTokenRepositoryImplTest {
  @Test
  fun `register POSTs the token and platform to the device-tokens endpoint`() =
    runTest {
      val env = TestEnv { respond("", HttpStatusCode.NoContent) }

      env.repository.register("tok-123", DevicePlatform.ANDROID)

      val request = env.requests.single()
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("http://test/api/v1/device-tokens", request.url.toString())
      assertEquals("""{"token":"tok-123","platform":"ANDROID"}""", request.body.bodyText())
    }

  @Test
  fun `unregister DELETEs the token from the device-tokens endpoint`() =
    runTest {
      val env = TestEnv { respond("", HttpStatusCode.NoContent) }

      env.repository.unregister("tok-123")

      val request = env.requests.single()
      assertEquals(HttpMethod.Delete, request.method)
      assertEquals("http://test/api/v1/device-tokens", request.url.toString())
      assertEquals("""{"token":"tok-123"}""", request.body.bodyText())
    }

  private class TestEnv(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
  ) {
    val requests: MutableList<HttpRequestData> = mutableListOf()
    val repository: DeviceTokenRepositoryImpl

    init {
      val client = HttpClient(
        MockEngine { request ->
          requests += request
          handler(request)
        }
      ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest { contentType(ContentType.Application.Json) }
        expectSuccess = true
      }
      repository = DeviceTokenRepositoryImpl(
        httpClient = client,
        networkConfig = NetworkConfig(baseUrl = "http://test", isDebug = true)
      )
    }
  }

  private companion object {
    fun OutgoingContent.bodyText(): String =
      when (this) {
        is TextContent -> text
        is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
        else -> error("Unsupported request body type: ${this::class}")
      }
  }
}
