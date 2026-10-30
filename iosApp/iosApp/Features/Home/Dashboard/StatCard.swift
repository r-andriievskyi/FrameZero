import SwiftUI
import FrameZero

/// One dashboard statistic; mirrors `StatCard` in composeApp/features/home `StatsSection`.
/// Without an `action` it is plain content rather than an inert button.
struct StatCard: View {
    let systemImage: String
    let value: Int32
    let label: LocalizedStringKey
    var action: (() -> Void)?
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        if let action {
            Button(action: action) { card }
                .buttonStyle(.plain)
        } else {
            card.accessibilityElement(children: .combine)
        }
    }

    private var card: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        return VStack(alignment: .leading, spacing: 0) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .accessibilityHidden(true)
                .padding(.bottom, CGFloat(spacing.space8))
            Text(value, format: .number)
                .textStyle(typography.displayMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .padding(.bottom, CGFloat(spacing.space4))
            Text(label)
                .textStyle(typography.bodySmall)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .padding(CGFloat(spacing.space16))
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color(argb: colors.cardBackground), in: shape)
        .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        .contentShape(shape)
    }
}

#Preview {
    HStack {
        StatCard(systemImage: "movieclapper", value: 3, label: "stats_active_projects")
        StatCard(systemImage: "checklist", value: 12, label: "stats_open_tasks") {}
    }
    .padding()
}
