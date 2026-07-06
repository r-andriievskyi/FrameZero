package com.frame.zero.domain

import com.frame.zero.auth.dto.UserDto
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMapperTest {
  @Test
  fun `toDomain copies every field`() {
    val dto = UserDto(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")

    val user = dto.toDomain()

    assertEquals(User(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace"), user)
  }

  @Test
  fun `toDomain preserves blank names`() {
    val dto = UserDto(id = "u2", email = "b@x.com", firstName = "", lastName = "")

    val user = dto.toDomain()

    assertEquals("", user.firstName)
    assertEquals("", user.lastName)
  }
}
