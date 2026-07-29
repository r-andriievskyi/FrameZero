package com.frame.zero.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A backgrounded task-create upload. [status]/[attemptCount]/[failureReason] are their own
 * columns so each retry can be recorded with a single atomic UPDATE (see
 * `PendingUploadDao.recordFailure`/`markUploading`); [payload] is the serialized *immutable*
 * task data (title, description, file info, …) and is never rewritten after [PendingUploadDao]'s
 * initial insert. Keeping the payload opaque there avoids per-field columns and Room
 * type-converters for dates/enums.
 */
@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
  @PrimaryKey val uploadId: String,
  val status: String,
  val attemptCount: Int = 0,
  val failureReason: String? = null,
  val payload: String
)
