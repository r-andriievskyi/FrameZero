package com.frame.zero.repository.productions

import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.production.ProductionSummaryDto

interface ProductionsRepository {
  suspend fun list(): PagedResponse<ProductionSummaryDto>
}
