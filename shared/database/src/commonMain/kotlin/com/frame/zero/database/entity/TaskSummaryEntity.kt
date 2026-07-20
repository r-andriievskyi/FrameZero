package com.frame.zero.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "task_summaries",
  indices = [Index(value = ["pageOrder"])]
)
data class TaskSummaryEntity(
  @PrimaryKey val id: String,
  val title: String,
  val productionTitle: String,
  val dueDateEpochDays: Long?,
  val status: String,
  val pageOrder: Long
)
