import SwiftUI
import FrameZero

typealias ScheduleTabModel = ScreenModel<ScheduleTabState, any ScheduleTabIntent>

extension ScheduleTabModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.scheduleTabViewModel
        self.init(viewModel, state: viewModel.state, onIntent: ScheduleTabViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/home `ScheduleTabContent`; accessibility identifiers =
/// `ScheduleTabTestTags`. The full-screen states apply only while nothing is cached — once a schedule
/// has loaded it stays on screen through reloads.
struct ScheduleTabView: View {
    let model: ScheduleTabModel
    let onTaskTap: (String) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        Group {
            if state.schedule == nil, let error = state.error {
                if error.autoRetries {
                    OfflineBanner(message: error.message.localized)
                        .accessibilityIdentifier("schedule:error")
                } else {
                    FullScreenError(message: error.message.localized) {
                        model.onIntent(ScheduleTabIntentRetry.shared)
                    }
                    .accessibilityIdentifier("schedule:error")
                }
            } else if state.schedule == nil && state.isLoading {
                FullScreenProgress()
                    .accessibilityIdentifier("schedule:loading")
            } else if let selectedDate = state.selectedDate {
                ScheduleContent(state: state, selectedDate: selectedDate, onIntent: model.onIntent, onTaskTap: onTaskTap)
                    .accessibilityIdentifier("schedule:content")
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .task { await model.observe() }
    }
}

private struct ScheduleContent: View {
    let state: ScheduleTabState
    let selectedDate: Kotlinx_datetimeLocalDate
    let onIntent: (any ScheduleTabIntent) -> Void
    let onTaskTap: (String) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        let daysWithItems = state.schedule?.daysWithItems() ?? []
        let selectDate = { (date: Kotlinx_datetimeLocalDate) in onIntent(ScheduleTabIntentDateSelected(date: date)) }
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("schedule_screen_title")
                    .textStyle(DesignTokens.shared.typography.displayMedium)
                    .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textPrimary))
                    .accessibilityAddTraits(.isHeader)
                    .padding(.bottom, CGFloat(spacing.space16))

                ScheduleViewSelector(selected: state.view) { onIntent(ScheduleTabIntentViewChanged(view: $0)) }
                    .padding(.bottom, CGFloat(spacing.space16))

                switch state.view {
                case .day:
                    ScheduleDateHeader(date: selectedDate, isToday: state.isSelectedDateToday)
                case .week:
                    WeekDayStrip(
                        days: ScheduleCalendarKt.weekOf(date: selectedDate),
                        selectedDate: selectedDate,
                        daysWithItems: daysWithItems,
                        onDayTap: selectDate
                    )
                case .month:
                    MonthCalendar(
                        year: state.displayYear,
                        month: state.displayMonth,
                        selectedDate: selectedDate,
                        daysWithItems: daysWithItems,
                        onDayTap: selectDate,
                        onMonthNavigated: { onIntent(ScheduleTabIntentMonthNavigated(offset: $0)) }
                    )
                }

                if !state.selectedDayEvents.isEmpty || !state.selectedDayTasks.isEmpty {
                    ScheduleTimeline(events: state.selectedDayEvents, tasks: state.selectedDayTasks, onTaskTap: onTaskTap)
                        .padding(.top, CGFloat(spacing.space24))
                }
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.top, CGFloat(spacing.space24))
        }
    }
}
