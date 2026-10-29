import SwiftUI
import FrameZero

private let iconContainerSize: CGFloat = 40

/// Tappable settings row with a trailing chevron; mirrors composeApp/features/account `SettingsRow`.
struct SettingsRow: View {
    let systemImage: String
    let title: LocalizedStringKey
    let subtitle: String
    let action: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Button(action: action) {
            HStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
                SettingsLabel(systemImage: systemImage, title: title, subtitle: Text(subtitle))
                Image(systemName: "chevron.right")
                    .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textMuted))
                    .accessibilityHidden(true)
            }
            .padding(CGFloat(DesignTokens.shared.spacing.space16))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// Settings row with a trailing switch instead of a chevron; mirrors composeApp/features/account
/// `SettingsToggleRow`. Toggling is the row's only action.
struct SettingsToggleRow: View {
    let systemImage: String
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey
    @Binding var isOn: Bool
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Toggle(isOn: $isOn) {
            SettingsLabel(systemImage: systemImage, title: title, subtitle: Text(subtitle))
        }
        .tint(Color(argb: ColorTokens.forScheme(colorScheme).accent))
        .padding(CGFloat(DesignTokens.shared.spacing.space16))
    }
}

/// Icon tile + title/subtitle shared by both row kinds.
private struct SettingsLabel: View {
    let systemImage: String
    let title: LocalizedStringKey
    let subtitle: Text
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(argb: colors.accent))
                .frame(width: iconContainerSize, height: iconContainerSize)
                .background(
                    Color(argb: colors.inputBackground),
                    in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
                )
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
                Text(title)
                    .textStyle(typography.bodyMedium)
                    .fontWeight(.medium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                subtitle
                    .textStyle(typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

#Preview {
    VStack(spacing: 0) {
        SettingsRow(systemImage: "person", title: "Edit profile", subtitle: "Maya Rivera") {}
        SettingsToggleRow(
            systemImage: "lock",
            title: "Biometric app lock",
            subtitle: "Require Face ID / fingerprint to open the app",
            isOn: .constant(true)
        )
    }
}
