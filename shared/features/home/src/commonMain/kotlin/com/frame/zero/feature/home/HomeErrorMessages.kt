package com.frame.zero.feature.home

import com.frame.zero.ui.DomainErrorCategory
import com.frame.zero.ui.ErrorStringKey
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText

/** Shared by [com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel] and
 *  [com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel] — both only need to override
 *  NETWORK, to explain the auto-retry-on-reconnect behavior; everything else is canonical text. */
internal val homeErrorMessages: Map<DomainErrorCategory, UiText> =
  mapOf(DomainErrorCategory.NETWORK to ErrorStringKey.HOME_OFFLINE.asUiText())
