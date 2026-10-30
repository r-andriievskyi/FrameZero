import SwiftUI
import FrameZero

private let height: CGFloat = 65
let floatingBottomNavOuterPadding = CGFloat(DesignTokens.shared.spacing.space16)
/// Bottom space tab content reserves so its last row can scroll clear of the nav.
let floatingBottomNavClearance = height + floatingBottomNavOuterPadding * 2

extension HomeTab {
    var label: LocalizedStringKey {
        switch self {
        case .dashboard: "tab_dashboard"
        case .productions: "tab_productions"
        case .schedule: "tab_schedule"
        }
    }
}

/// Pill-shaped tab switcher floating above the content; mirrors composeApp/features/home
/// `FloatingBottomNav`. Labels double as Maestro text anchors ("Productions").
struct FloatingBottomNav: View {
    @Binding var selection: HomeTab
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        HStack(spacing: 0) {
            ForEach(HomeTab.allCases, id: \.self) { tab in
                let isSelected = tab == selection
                Button {
                    withAnimation { selection = tab }
                } label: {
                    Text(tab.label)
                        .textStyle(DesignTokens.shared.typography.labelMedium)
                        .foregroundStyle(Color(argb: isSelected ? colors.textOnAccent : colors.textSecondary))
                        .padding(.horizontal, CGFloat(spacing.space16))
                        .padding(.vertical, CGFloat(spacing.space8))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(isSelected ? Color(argb: colors.accent) : .clear, in: Capsule())
                        .contentShape(Capsule())
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(isSelected ? [.isSelected] : [])
            }
        }
        .padding(CGFloat(spacing.space4))
        .frame(height: height)
        .background(Color(argb: colors.surfaceElevated), in: Capsule())
        .overlay(Capsule().stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

#Preview {
    @Previewable @State var selection = HomeTab.dashboard
    FloatingBottomNav(selection: $selection)
        .padding()
}
