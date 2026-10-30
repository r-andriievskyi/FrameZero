import SwiftUI
import FrameZero

/// Muted section title over a bordered card; mirrors composeApp/features/account `SettingsSection`.
struct SettingsSection<Content: View>: View {
    let title: LocalizedStringKey
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text(title)
                .textStyle(DesignTokens.shared.typography.labelLarge)
                .fontWeight(.semibold)
                .foregroundStyle(Color(argb: colors.textMuted))
                .accessibilityAddTraits(.isHeader)
            VStack(spacing: 0) {
                content
            }
            .background(Color(argb: colors.cardBackground), in: shape)
            .clipShape(shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        }
    }
}

#Preview {
    SettingsSection(title: "Account") {
        SettingsRow(systemImage: "person", title: "Edit profile", subtitle: "Maya Rivera") {}
    }
    .padding()
}
