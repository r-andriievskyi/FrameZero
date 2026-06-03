package com.frame.zero.feature.home.tab.productions

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.repository.productions.ProductionsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

class ProductionsTabViewModel(
  productionsRepository: ProductionsRepository,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  val productions: Flow<PagingData<ProductionUi>> = productionsRepository
    .observeProductions()
    .map { page -> page.map { it.toUi() } }
    .cachedIn(scope)

  override fun onDestroy() {
    scope.cancel()
  }
}
