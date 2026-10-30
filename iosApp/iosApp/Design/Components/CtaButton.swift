import SwiftUI
import FrameZero

private let height: CGFloat = 48

/// Filled accent call-to-action; mirrors Compose's `CtaButton` (design_system). While `isLoading`
/// it shows a spinner and ignores taps.
struct CtaButton: View {
    let title: LocalizedStringKey
    var isLoading = false
    let action: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(Color(argb: colors.textOnAccent))
                } else {
                    Text(title)
                        .textStyle(DesignTokens.shared.typography.labelLarge)
                        .foregroundStyle(Color(argb: colors.textOnAccent))
                }
            }
            .frame(maxWidth: .infinity, minHeight: height)
            .background(
                Color(argb: isLoading ? colors.accentDim : colors.accent),
                in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
            )
        }
        .buttonStyle(.plain)
        .disabled(isLoading)
        .accessibilityLabel(Text(title))
    }
}

/// Transparent call-to-action outlined in its content color; mirrors Compose's `OutlinedCtaButton`
/// (design_system).
struct OutlinedCtaButton: View {
    let title: LocalizedStringKey
    let color: Color
    let action: () -> Void

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        Button(action: action) {
            Text(title)
                .textStyle(DesignTokens.shared.typography.labelLarge)
                .foregroundStyle(color)
                .frame(maxWidth: .infinity, minHeight: height)
                .overlay(shape.stroke(color, lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
                .contentShape(shape)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    VStack(spacing: 12) {
        CtaButton(title: "Continue") {}
        CtaButton(title: "Continue", isLoading: true) {}
        OutlinedCtaButton(title: "Sign out", color: .red) {}
    }
    .padding()
}
