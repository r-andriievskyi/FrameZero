package com.frame.zero.auth.dto

import com.frame.zero.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
  val email: String,
  val password: String,
  val firstName: String,
  val lastName: String,
)

@Serializable data class LoginRequest(val email: String, val password: String)

@Serializable data class RefreshRequest(val refreshToken: String)

@Serializable data class LogoutRequest(val refreshToken: String)

@Serializable data class UserDto(val id: String, val email: String, val firstName: String, val lastName: String)

fun UserDto.toDomain(): User = User(id = id, email = email, firstName = firstName, lastName = lastName)

@Serializable
data class AuthResponse(val accessToken: String, val refreshToken: String, val user: UserDto)

@Serializable data class RefreshResponse(val accessToken: String, val refreshToken: String)
