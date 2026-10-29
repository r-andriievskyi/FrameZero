import SwiftUI
import FrameZero

/// Seven tappable day cells for Week view; mirrors composeApp/features/home `WeekDayStrip`.
struct WeekDayStrip: View {
    let days: [Kotlinx_datetimeLocalDate]
    let selectedDate: Kotlinx_datetimeLocalDate
    let daysWithItems: Set<Kotlinx_datetimeLocalDate>
    let onDayTap: (Kotlinx_datetimeLocalDate) -> Void

    var body: some View {
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
            ForEach(days, id: \.self) { date in
                WeekDayCell(
                    date: date,
                    isSelected: date == selectedDate,
                    hasItems: daysWithItems.contains(date)
                ) { onDayTap(date) }
            }
        }
    }
}

private struct WeekDayCell: View {
    let date: Kotlinx_datetimeLocalDate
    let isSelected: Bool
    let hasItems: Bool
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        // No pressed feedback, as on Android: selection is shown by the fill.
        Button(action: onTap) {
            VStack(spacing: 0) {
                Text(date.dayOfWeek.shortName().uppercased())
                    .textStyle(DesignTokens.shared.typography.labelSmall)
                    .foregroundStyle(Color(argb: isSelected ? colors.textOnAccent : colors.textMuted))
                    .padding(.bottom, CGFloat(spacing.space4))
                Text("\(date.day)")
                    .textStyle(DesignTokens.shared.typography.titleMedium)
                    .foregroundStyle(Color(argb: isSelected ? colors.textOnAccent : colors.textPrimary))
                    .padding(.bottom, CGFloat(spacing.space2))
                EventDot(isVisible: hasItems, onAccent: isSelected)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, CGFloat(spacing.space8))
            .background(Color(argb: isSelected ? colors.accent : colors.cardBackground), in: shape)
            .overlay(
                shape.stroke(
                    Color(argb: isSelected ? colors.accent : colors.cardBorder),
                    lineWidth: CGFloat(DesignTokens.shared.border.hairline)
                )
            )
            .contentShape(shape)
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private let eventDotSize: CGFloat = 6

/// Marks a day with events or tasks; keeps its space when hidden so cells line up.
struct EventDot: View {
    let isVisible: Bool
    let onAccent: Bool
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        Circle()
            .fill(Color(argb: onAccent ? colors.textOnAccent : colors.accent))
            .frame(width: eventDotSize, height: eventDotSize)
            .opacity(isVisible ? 1 : 0)
            .accessibilityHidden(true)
    }
}
