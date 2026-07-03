package com.frame.zero.feature.task.details

/** A production member selectable as a task participant, shown in the participants picker. */
data class AssignableMemberUi(
  val userId: String,
  val name: String,
  val initials: String,
  val avatarColorHex: String?
)
