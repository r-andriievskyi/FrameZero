package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.repository.user.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class UserRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : UserRepository {
  override suspend fun getMe(): UserDto = httpClient.get("${networkConfig.baseUrl}/auth/me").body()
}
