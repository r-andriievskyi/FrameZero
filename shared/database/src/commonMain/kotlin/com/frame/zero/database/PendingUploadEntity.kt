package com.frame.zero.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A backgrounded task-create upload. [status] is its own column so it can be flipped with a
 * single atomic UPDATE; [payload] is the serialized domain record (everything else). Keeping
 * the payload opaque avoids per-field columns and Room type-converters for dates/enums.
 */
@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
  @PrimaryKey val uploadId: String,
  val status: String,
  val payload: String
)
