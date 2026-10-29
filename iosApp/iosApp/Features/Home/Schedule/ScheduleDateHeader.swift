import SwiftUI
import FrameZero

/// The selected day in Day view, e.g. "Sunday,  Apr  26, 2026" + a Today badge; mirrors
/// composeApp/features/home `ScheduleDateHeader` (same shared English day/month names).
struct ScheduleDateHeader: View {
    let date: Kotlinx_datetimeLocalDate
    let isToday: Bool
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        HStack(spacing: CGFloat(spacing.space8)) {
            Text("\(date.dayOfWeek.fullName()),  \(date.formatMonthShort())  \(date.day), \(String(date.year))")
                .textStyle(DesignTokens.shared.typography.monoMedium)
                .foregroundStyle(Color(argb: colors.textSecondary))
            if isToday {
                let capsule = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radiusMax))
                Text("today")
                    .textStyle(DesignTokens.shared.typography.labelSmall)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .padding(.horizontal, CGFloat(spacing.space8))
                    .padding(.vertical, CGFloat(spacing.space4))
                    .overlay(capsule.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            }
        }
        .accessibilityElement(children: .combine)
    }
}
