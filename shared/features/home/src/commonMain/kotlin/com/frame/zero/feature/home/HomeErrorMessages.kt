package com.frame.zero.feature.home

import com.frame.zero.ui.DomainErrorCategory
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.home.generated.resources.Res
import framezero.shared.features.home.generated.resources.error_offline_message

/** Shared by [com.frame.zero.feature.home.tab.dashboard.DashboardTabViewModel] and
 *  [com.frame.zero.feature.home.tab.schedule.ScheduleTabViewModel] — both only need to override
 *  NETWORK, to explain the auto-retry-on-reconnect behavior; everything else is canonical text. */
internal val homeErrorMessages: Map<DomainErrorCategory, UiText> =
  mapOf(DomainErrorCategory.NETWORK to Res.string.error_offline_message.asUiText())
