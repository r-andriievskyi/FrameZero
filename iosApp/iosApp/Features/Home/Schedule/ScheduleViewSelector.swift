import SwiftUI
import FrameZero

/// Day / Week / Month segmented control; mirrors composeApp/features/home `ScheduleViewSelector`.
struct ScheduleViewSelector: View {
    let selected: ScheduleView
    let onSelect: (ScheduleView) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let capsule = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radiusMax))
        HStack(spacing: 0) {
            ForEach(ScheduleView.allCases, id: \.self) { view in
                let isSelected = view == selected
                Button { onSelect(view) } label: {
                    Text(view.label)
                        .textStyle(DesignTokens.shared.typography.labelMedium)
                        .foregroundStyle(Color(argb: isSelected ? colors.textOnAccent : colors.textSecondary))
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, CGFloat(spacing.space16))
                        .padding(.vertical, CGFloat(spacing.space8))
                        .background(Color(argb: isSelected ? colors.accent : colors.inputBackground), in: capsule)
                        .contentShape(capsule)
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
        .padding(CGFloat(spacing.space4))
        .background(Color(argb: colors.inputBackground), in: capsule)
        .animation(.easeInOut(duration: 0.2), value: selected)
    }
}

private extension ScheduleView {
    var label: LocalizedStringKey {
        switch self {
        case .day: "schedule_view_day"
        case .week: "schedule_view_week"
        case .month: "schedule_view_month"
        }
    }
}
