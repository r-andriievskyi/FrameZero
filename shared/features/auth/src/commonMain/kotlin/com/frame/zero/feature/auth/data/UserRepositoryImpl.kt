package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.User
import com.frame.zero.domain.toDomain
import com.frame.zero.repository.user.UserRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class UserRepositoryImpl(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig
) : UserRepository {
  override suspend fun getMe(): User =
    httpClient
      .get("${networkConfig.baseUrl}/auth/me")
      .body<UserDto>()
      .toDomain()
}
