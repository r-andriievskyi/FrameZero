import SwiftUI
import FrameZero

/// Initials on a circle in the member's color (`#RRGGBB`), falling back to `accentDim`; mirrors the
/// member avatars in composeApp/features/task-details and task-create.
struct MemberAvatar: View {
    let initials: String
    let colorHex: String?
    let size: CGFloat
    var style: TextStyleToken = DesignTokens.shared.typography.labelSmall
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        Text(initials)
            .textStyle(style)
            .foregroundStyle(Color(argb: colors.textOnAccent))
            .frame(width: size, height: size)
            .background(colorHex.flatMap(Color.init(rgbHex:)) ?? Color(argb: colors.accentDim), in: Circle())
            .accessibilityHidden(true)
    }
}

private extension Color {
    /// `#RRGGBB` (the leading `#` optional), opaque; nil when malformed.
    init?(rgbHex: String) {
        let digits = rgbHex.hasPrefix("#") ? String(rgbHex.dropFirst()) : rgbHex
        guard digits.count == 6, let rgb = UInt32(digits, radix: 16) else { return nil }
        self.init(argb: Int64(0xFF00_0000 | rgb))
    }
}
