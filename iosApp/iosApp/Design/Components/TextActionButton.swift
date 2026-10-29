import SwiftUI
import FrameZero

/// Muted text-only secondary action under a primary `CtaButton` ("Later", "Sign out instead");
/// mirrors the clickable `labelLarge` text Compose uses for these.
struct TextActionButton: View {
    let title: LocalizedStringKey
    let action: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        Button(action: action) {
            Text(title)
                .textStyle(DesignTokens.shared.typography.labelLarge)
                .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).textMuted))
                .padding(.horizontal, CGFloat(spacing.space16))
                .padding(.vertical, CGFloat(spacing.space8))
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    TextActionButton(title: "Later") {}
}
