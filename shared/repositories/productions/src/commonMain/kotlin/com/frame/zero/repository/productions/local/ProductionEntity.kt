package com.frame.zero.repository.productions.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "productions",
  indices = [Index(value = ["pageOrder"])]
)
data class ProductionEntity(
  @PrimaryKey val id: String,
  val title: String,
  val genre: String,
  val phase: String,
  val progressPercent: Int,
  val daysLeft: Int,
  val membersCount: Int,
  val updatedAtEpochMs: Long,
  val pageOrder: Long
)
