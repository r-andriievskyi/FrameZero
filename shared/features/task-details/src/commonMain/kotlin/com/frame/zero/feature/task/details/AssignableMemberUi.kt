@file:OptIn(ExperimentalObjCName::class)

package com.frame.zero.feature.task.details

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * A production member selectable as a task participant, shown in the participants picker.
 * Explicit Swift name: task-create declares an `AssignableMemberUi` too.
 */
@ObjCName("TaskDetailsMember")
data class AssignableMemberUi(
  val userId: String,
  val name: String,
  val initials: String,
  val avatarColorHex: String?
)
