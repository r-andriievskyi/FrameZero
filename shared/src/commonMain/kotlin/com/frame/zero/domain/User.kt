package com.frame.zero.domain

data class User(
  val id: String,
  val email: String,
  val firstName: String = "",
  val lastName: String = ""
)
