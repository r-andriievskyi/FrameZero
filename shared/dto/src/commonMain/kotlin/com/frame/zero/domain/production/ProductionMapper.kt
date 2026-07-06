package com.frame.zero.domain.production

import com.frame.zero.dto.production.CreateCrewMemberDto
import com.frame.zero.dto.production.CreateProductionRequest

fun NewProduction.toCreateRequest(): CreateProductionRequest =
  CreateProductionRequest(
    title = title,
    genre = genre,
    logline = logline,
    startDate = startDate,
    wrapDate = wrapDate,
    budgetCents = budgetCents,
    crew = crew.map { it.toDto() }
  )

fun NewCrewMember.toDto(): CreateCrewMemberDto = CreateCrewMemberDto(name = name, role = role, email = email)
