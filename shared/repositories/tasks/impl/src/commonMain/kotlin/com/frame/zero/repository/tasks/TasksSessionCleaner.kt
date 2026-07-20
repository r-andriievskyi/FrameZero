package com.frame.zero.repository.tasks

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.dao.TaskSummariesDao

internal class TasksSessionCleaner(
  private val dao: TaskSummariesDao
) : SessionCleaner {
  override suspend fun clear() {
    dao.deleteAll()
  }
}
