package com.frame.zero.schedule

import com.frame.zero.production.ProductionsTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ScheduleEventsTable : Table("schedule_events") {
  val id = javaUUID("id")
  val productionId = javaUUID("production_id").references(ProductionsTable.id)
  val title = varchar("title", 200)
  val location = varchar("location", 300).nullable()
  val startsAt = timestamp("starts_at")
  val endsAt = timestamp("ends_at")
  val kind = varchar("kind", 20)

  override val primaryKey = PrimaryKey(id)

  init {
    index("idx_events_production", false, productionId)
    index("idx_events_starts_at", false, startsAt)
  }
}
