package com.frame.zero.repository.tasks

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.dao.TaskSummariesDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class)
@Inject
class TasksSessionCleaner(
  private val dao: TaskSummariesDao
) : SessionCleaner {
  override suspend fun clear() {
    dao.deleteAll()
  }
}
