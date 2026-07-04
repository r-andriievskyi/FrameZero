package com.frame.zero.chat

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.production.AccessLevel
import com.frame.zero.production.ProductionAccessService
import com.frame.zero.task.TaskRecord
import com.frame.zero.task.TaskRepository
import com.frame.zero.task.circleUserIds
import java.util.UUID

/**
 * Chat's authorization boundary. Access to a task's chat is the **task circle** —
 * creator + current assignee + task participants — not all production members. A
 * production-membership check sits underneath as defense in depth. Evaluated from
 * the DB on every operation (never cached in a socket session), so removing a user
 * from the task cuts off their chat access at the next request.
 */
class TaskCircleAccessService(
  private val tasks: TaskRepository,
  private val access: ProductionAccessService
) {
  /** Throws [AppError.NotFound] if the task is gone, [AppError.Forbidden] if the caller is outside the circle. */
  suspend fun requireCircle(
    userId: UUID,
    taskId: UUID
  ): TaskRecord {
    val task = tasks.findById(taskId) ?: throw AppException(AppError.NotFound)
    // Defense in depth: a task-circle member is always a production member too, but
    // check membership explicitly so a stale circle can never outlive membership.
    access.requireAccess(userId, task.productionId, AccessLevel.READ)
    if (userId !in task.circleUserIds()) throw AppException(AppError.Forbidden)
    return task
  }

  /** Non-throwing variant for the WebSocket SUBSCRIBE path. */
  suspend fun isInCircle(
    userId: UUID,
    taskId: UUID
  ): Boolean {
    val task = tasks.findById(taskId) ?: return false
    if (userId !in task.circleUserIds()) return false
    return runCatching { access.requireAccess(userId, task.productionId, AccessLevel.READ) }.isSuccess
  }
}
