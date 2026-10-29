import SwiftUI
import FrameZero

private let iconSize: CGFloat = 20
private let borderOpacity = 0.22

enum ToastSeverity {
    case success, warning, error
}

/// Soft toast card: severity icon, title, optional description and optional trailing action;
/// mirrors Compose's `AppToast` (design_system).
struct AppToast: View {
    let title: String
    let severity: ToastSeverity
    var description: String?
    var actionLabel: LocalizedStringKey?
    var onAction: (() -> Void)?
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let (surface, text, icon) = style(colors)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius14))
        HStack(alignment: description == nil ? .center : .top, spacing: CGFloat(spacing.space12)) {
            Image(systemName: icon)
                .resizable()
                .frame(width: iconSize, height: iconSize)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: CGFloat(spacing.space2)) {
                Text(title)
                    .textStyle(typography.titleMedium)
                if let description {
                    Text(description)
                        .textStyle(typography.bodySmall)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            if let actionLabel, let onAction {
                Button(action: onAction) {
                    Text(actionLabel)
                        .textStyle(typography.labelMedium)
                        .padding(CGFloat(spacing.space4))
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .frame(maxHeight: .infinity)
            }
        }
        .foregroundStyle(text)
        .fixedSize(horizontal: false, vertical: true)
        .padding(.horizontal, CGFloat(spacing.space16))
        .padding(.vertical, CGFloat(spacing.space12))
        .background(surface, in: shape)
        .overlay(shape.stroke(text.opacity(borderOpacity), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }

    /// Surface, content color and icon per severity; mirrors `toastColors`.
    private func style(_ colors: ColorTokens) -> (Color, Color, String) {
        switch severity {
        case .success: (Color(argb: colors.successSurface), Color(argb: colors.successText), "checkmark.circle")
        case .warning: (Color(argb: colors.warningSurface), Color(argb: colors.warningText), "exclamationmark.circle")
        case .error: (Color(argb: colors.errorSurface), Color(argb: colors.errorText), "xmark.circle")
        }
    }
}

#Preview {
    VStack(spacing: 8) {
        AppToast(title: "Scene 12 saved to cloud", severity: .success)
        AppToast(title: "Call sheet not sent", severity: .warning, description: "Send before 9pm.")
        AppToast(title: "Upload failed", severity: .error, actionLabel: "Retry") {}
    }
    .padding()
}
