import SwiftUI
import FrameZero

/// Blocking cover for a build below the minimum; mirrors composeApp `HardUpdateScreen`. No dismiss —
/// the only action opens the store.
struct HardUpdateView: View {
    let onUpdate: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        UpdateLayout(
            badge: UpdateLogoBadge(
                badgeSystemImage: "exclamationmark.triangle",
                badgeBackground: Color(argb: colors.warningSurface),
                badgeTint: Color(argb: colors.warningText)
            ),
            title: "update_hard_title",
            subtitle: "update_hard_subtitle"
        ) {
            Text("update_hard_warning")
                .textStyle(DesignTokens.shared.typography.bodySmall)
                .foregroundStyle(Color(argb: colors.warningText))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(CGFloat(spacing.space16))
                .background(
                    Color(argb: colors.warningSurface),
                    in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
                )
                .padding(.bottom, CGFloat(spacing.space32))
            CtaButton(title: "update_hard_action_button", action: onUpdate)
        }
    }
}

/// Dismissible prompt for a newer build; mirrors composeApp `SoftUpdateScreen`.
struct SoftUpdateView: View {
    let onUpdate: () -> Void
    let onDismiss: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        UpdateLayout(
            badge: UpdateLogoBadge(
                badgeSystemImage: "arrow.down",
                badgeBackground: Color(argb: colors.accentSurface),
                badgeTint: Color(argb: colors.accentText)
            ),
            title: "update_soft_title",
            subtitle: "update_soft_subtitle"
        ) {
            CtaButton(title: "update_action_button", action: onUpdate)
                .padding(.bottom, CGFloat(DesignTokens.shared.spacing.space8))
            TextActionButton(title: "update_later_button", action: onDismiss)
        }
    }
}

/// Shared shape of both update covers: badge + copy centered in the free space, actions at the
/// bottom, opaque and hit-testable over the whole window.
private struct UpdateLayout<Actions: View>: View {
    let badge: UpdateLogoBadge
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey
    @ViewBuilder let actions: Actions
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(spacing: 0) {
            VStack(spacing: 0) {
                badge
                    .padding(.bottom, CGFloat(spacing.space24))
                Text(title)
                    .textStyle(typography.displayMedium)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityAddTraits(.isHeader)
                    .padding(.bottom, CGFloat(spacing.space8))
                Text(subtitle)
                    .textStyle(typography.bodyMedium)
                    .foregroundStyle(Color(argb: colors.textMuted))
            }
            .multilineTextAlignment(.center)
            .frame(maxHeight: .infinity)
            actions
        }
        .padding(.horizontal, CGFloat(spacing.space32))
        .padding(.vertical, CGFloat(spacing.space24))
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: colors.background))
        .contentShape(Rectangle())
    }
}

#Preview("Hard") {
    HardUpdateView(onUpdate: {})
}

#Preview("Soft") {
    SoftUpdateView(onUpdate: {}, onDismiss: {})
}
