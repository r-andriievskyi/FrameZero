package com.frame.zero.feature.home.tab.productions

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase

data class ProductionUi(
  val id: String,
  val title: String,
  val genre: Genre,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val membersCount: Int
)

fun Production.toUi(): ProductionUi =
  ProductionUi(
    id = id,
    title = title,
    genre = genre,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    membersCount = membersCount
  )
