package com.frame.zero.repository.tasks.network

import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.task.TaskSummaryDto

interface TasksApi {
  suspend fun getAll(
    limit: Int,
    cursor: String?
  ): CursorPagedResponse<TaskSummaryDto>
}
