import SwiftUI
import FrameZero

private let minHeight: CGFloat = 48
private let multilineMinHeight: CGFloat = 96

/// Single-line text input; mirrors Compose's `SingleLineInputField` (design_system).
/// `isSecure` masks the text; `showsRevealToggle` adds the eye button that unmasks it.
/// `accessibilityID` lands on the text field itself, where Maestro's `id:` selectors expect it.
struct InputField: View {
    let placeholder: LocalizedStringResource
    @Binding var text: String
    var systemImage: String?
    /// Literal text before the input, e.g. a currency sign.
    var prefix: String?
    var isSecure = false
    var showsRevealToggle = false
    /// Grows downward from a 96 pt box; mirrors task-create's `MultiLineInputField`.
    var isMultiline = false
    var accessibilityID: String?
    @State private var isRevealed = false
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        // Verbatim: a `LocalizedStringKey` would render "you@studio.com" as a mail link.
        let placeholderText = String(localized: placeholder)
        let prompt = Text(verbatim: placeholderText).foregroundStyle(Color(argb: colors.textMuted))

        HStack(spacing: CGFloat(spacing.space8)) {
            if let systemImage {
                Image(systemName: systemImage)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityHidden(true)
            }
            if let prefix {
                Text(verbatim: prefix)
                    .textStyle(DesignTokens.shared.typography.bodyLarge)
                    .foregroundStyle(Color(argb: colors.textMuted))
                    .accessibilityHidden(true)
            }
            Group {
                if isSecure && !isRevealed {
                    SecureField(text: $text, prompt: prompt) { Text(verbatim: placeholderText) }
                } else {
                    TextField(text: $text, prompt: prompt, axis: isMultiline ? .vertical : .horizontal) {
                        Text(verbatim: placeholderText)
                    }
                }
            }
            .textStyle(DesignTokens.shared.typography.bodyLarge)
            .foregroundStyle(Color(argb: colors.textPrimary))
            .tint(Color(argb: colors.accent))
            .accessibilityIdentifier(accessibilityID ?? "")
            if isSecure && showsRevealToggle {
                Button {
                    isRevealed.toggle()
                } label: {
                    Image(systemName: isRevealed ? "eye.slash" : "eye")
                        .foregroundStyle(Color(argb: colors.textPrimary))
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("cd_password_visibility"))
                .accessibilityValue(Text(isRevealed ? "state_password_visible" : "state_password_hidden"))
            }
        }
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.vertical, CGFloat(isMultiline ? spacing.space12 : spacing.space8))
        .frame(
            maxWidth: .infinity,
            minHeight: isMultiline ? multilineMinHeight : minHeight,
            alignment: isMultiline ? .topLeading : .leading
        )
        .background(Color(argb: colors.inputBackground), in: shape)
        .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

#Preview {
    VStack(spacing: 12) {
        InputField(placeholder: "placeholder_email", text: .constant(""), systemImage: "envelope")
        InputField(
            placeholder: "placeholder_password",
            text: .constant("secret"),
            systemImage: "lock",
            isSecure: true,
            showsRevealToggle: true
        )
    }
    .padding()
}
