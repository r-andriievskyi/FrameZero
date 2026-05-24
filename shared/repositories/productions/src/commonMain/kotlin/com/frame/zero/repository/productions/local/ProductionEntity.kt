package com.frame.zero.repository.productions.local

import androidx.room.Entity

@Entity(
  tableName = "productions",
  primaryKeys = ["id", "phaseFilter"]
)
data class ProductionEntity(
  val id: String,
  val phaseFilter: String,
  val title: String,
  val genre: String,
  val phase: String,
  val progressPercent: Int,
  val daysLeft: Int,
  val membersCount: Int,
  val updatedAtEpochMs: Long,
  val pageOrder: Long
)
