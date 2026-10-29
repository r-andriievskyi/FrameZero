import SwiftUI
import FrameZero

/// Mirrors composeApp/features/home `HomeToolbar`. The logo shows as the app name until an iOS
/// logo asset exists.
struct HomeToolbar: View {
    let onNotifications: () -> Void
    let onAccount: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        HStack(spacing: CGFloat(spacing.space8)) {
            Text("app_name")
                .textStyle(DesignTokens.shared.typography.titleLarge)
                .foregroundStyle(Color(argb: colors.accent))
            Spacer()
            Button(action: onNotifications) {
                Image(systemName: "bell")
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .padding(CGFloat(spacing.space8))
                    .contentShape(shape)
            }
            .accessibilityLabel(Text("toolbar_notifications_cd"))
            Button(action: onAccount) {
                Image(systemName: "person")
                    .foregroundStyle(Color(argb: colors.textOnAccent))
                    .padding(CGFloat(spacing.space8))
                    .background(Color(argb: colors.accent), in: shape)
            }
            // Maestro's sign-out flow taps this by its label ("Settings").
            .accessibilityLabel(Text("toolbar_settings_cd"))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.vertical, CGFloat(spacing.space8))
    }
}

#Preview {
    HomeToolbar(onNotifications: {}, onAccount: {})
}
