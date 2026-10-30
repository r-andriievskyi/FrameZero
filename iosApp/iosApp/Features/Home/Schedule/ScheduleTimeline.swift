import SwiftUI
import FrameZero

/// The selected day's events (time-labelled) and due tasks; mirrors composeApp/features/home
/// `ScheduleTimeline`.
struct ScheduleTimeline: View {
    let events: [ScheduleEventUiModel]
    let tasks: [ScheduleTaskUiModel]
    let onTaskTap: (String) -> Void

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        VStack(alignment: .leading, spacing: 0) {
            if !events.isEmpty {
                ScheduleSectionHeader(systemImage: "calendar.badge.clock", title: "schedule_events_header", count: events.count)
                    .padding(.bottom, CGFloat(spacing.space16))
                VStack(spacing: CGFloat(spacing.space16)) {
                    ForEach(events, id: \.id) { event in
                        TimelineRow(timeLabel: event.timeRangeLabel) {
                            ScheduleEventCard(event: event)
                        }
                    }
                }
            }
            if !tasks.isEmpty {
                ScheduleSectionHeader(systemImage: "checklist", title: "schedule_tasks_due_header", count: tasks.count)
                    .padding(.top, events.isEmpty ? 0 : CGFloat(spacing.space24))
                    .padding(.bottom, CGFloat(spacing.space16))
                VStack(spacing: CGFloat(spacing.space8)) {
                    ForEach(tasks, id: \.id) { task in
                        ScheduleTaskCard(task: task) { onTaskTap(task.id) }
                    }
                }
            }
        }
    }
}

private struct ScheduleSectionHeader: View {
    let systemImage: String
    let title: LocalizedStringKey
    let count: Int
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(argb: colors.accent))
                .accessibilityHidden(true)
            Text(title)
                .textStyle(typography.labelLarge)
                .foregroundStyle(Color(argb: colors.textPrimary))
            Text("\(count)")
                .textStyle(typography.labelLarge)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }
}

private struct TimelineRow<Content: View>: View {
    let timeLabel: String
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            Text(timeLabel)
                .textStyle(DesignTokens.shared.typography.monoSmall)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textMuted))
            content.frame(maxWidth: .infinity)
        }
    }
}
