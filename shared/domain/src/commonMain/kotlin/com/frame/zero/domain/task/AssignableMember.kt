package com.frame.zero.domain.task

import kotlinx.serialization.Serializable

@Serializable
data class AssignableMember(
  val userId: String,
  val name: String,
  val initials: String,
  val avatarColorHex: String?
)
