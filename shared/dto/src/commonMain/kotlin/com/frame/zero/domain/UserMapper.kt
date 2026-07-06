package com.frame.zero.domain

import com.frame.zero.auth.dto.UserDto

fun UserDto.toDomain(): User = User(id = id, email = email, firstName = firstName, lastName = lastName)
