import SwiftUI
import FrameZero

private let dayCellSize: CGFloat = 44
private let navButtonSize: CGFloat = 36
private let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)

/// Month view: previous/next navigation, weekday header and a Monday-first day grid (the shared
/// `monthGrid`); mirrors composeApp/features/home `MonthCalendar`.
///
/// Android also takes a `today` highlight, but passes the selected date for it, so it never shows
/// separately from the selection — left out here.
struct MonthCalendar: View {
    let year: Int32
    let month: Kotlinx_datetimeMonth
    let selectedDate: Kotlinx_datetimeLocalDate
    let daysWithItems: Set<Kotlinx_datetimeLocalDate>
    let onDayTap: (Kotlinx_datetimeLocalDate) -> Void
    let onMonthNavigated: (Int32) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let grid = ScheduleCalendarKt.monthGrid(year: year, month: month)
        VStack(spacing: 0) {
            HStack {
                MonthNavButton(systemImage: "chevron.left", label: "cd_previous_month") { onMonthNavigated(-1) }
                Spacer()
                Text("\(month.fullName()) \(String(year))")
                    .textStyle(DesignTokens.shared.typography.titleMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                Spacer()
                MonthNavButton(systemImage: "chevron.right", label: "cd_next_month") { onMonthNavigated(1) }
            }
            .padding(.bottom, CGFloat(spacing.space16))

            LazyVGrid(columns: columns, spacing: CGFloat(spacing.space4)) {
                // Keyed by position: the labels repeat ("T", "S").
                ForEach(Array(DateFormatKt.calendarDayLabels.enumerated()), id: \.offset) { _, label in
                    Text(label)
                        .textStyle(DesignTokens.shared.typography.labelSmall)
                        .foregroundStyle(Color(argb: colors.textMuted))
                        .frame(height: dayCellSize)
                }
            }
            .accessibilityHidden(true) // Each day cell reads its full date.
            .padding(.bottom, CGFloat(spacing.space4))

            LazyVGrid(columns: columns, spacing: 0) {
                ForEach(0..<Int(grid.leadingBlanks), id: \.self) { _ in
                    Color.clear.frame(width: dayCellSize, height: dayCellSize)
                }
                ForEach(grid.days, id: \.self) { date in
                    MonthDayCell(
                        date: date,
                        isSelected: date == selectedDate,
                        hasItems: daysWithItems.contains(date)
                    ) { onDayTap(date) }
                }
            }
        }
    }
}

private struct MonthNavButton: View {
    let systemImage: String
    let label: LocalizedStringKey
    let action: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        Button(action: action) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .frame(width: navButtonSize, height: navButtonSize)
                .background(Color(argb: colors.inputBackground), in: shape)
                .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
                // 44 pt touch target around the 36 pt button, like Compose's minimumInteractiveComponentSize.
                .frame(width: dayCellSize, height: dayCellSize)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(label))
    }
}

private struct MonthDayCell: View {
    let date: Kotlinx_datetimeLocalDate
    let isSelected: Bool
    let hasItems: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        let textColor = isSelected ? colors.textOnAccent : hasItems ? colors.accentText : colors.textPrimary
        // No pressed feedback, as on Android: selection is shown by the fill.
        Button(action: onTap) {
            VStack(spacing: CGFloat(DesignTokens.shared.spacing.space2)) {
                Text("\(date.day)")
                    .textStyle(DesignTokens.shared.typography.bodyMedium)
                    .foregroundStyle(Color(argb: textColor))
                if hasItems {
                    EventDot(isVisible: true, onAccent: isSelected)
                }
            }
            .frame(width: dayCellSize, height: dayCellSize)
            .background(isSelected ? Color(argb: colors.accent) : Color.clear, in: shape)
            .contentShape(shape)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(date.formatMedium()))
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
