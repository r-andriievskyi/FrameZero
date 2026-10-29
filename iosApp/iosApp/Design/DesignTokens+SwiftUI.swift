import SwiftUI
import FrameZero

// SwiftUI view of the shared `DesignTokens` (shared/design-tokens) — the same values Compose's
// `ThemeOptions` reads, so a token added there shows up on both platforms.

extension Color {
    /// `0xAARRGGBB`, the encoding `ColorTokens` uses.
    init(argb: Int64) {
        let value = UInt32(truncatingIfNeeded: argb)
        self.init(
            .sRGB,
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255,
            opacity: Double((value >> 24) & 0xFF) / 255
        )
    }
}

extension ColorTokens {
    static func forScheme(_ scheme: ColorScheme) -> ColorTokens {
        scheme == .dark ? DesignTokens.shared.dark : DesignTokens.shared.light
    }
}

extension TextStyleToken {
    var font: Font {
        .system(
            size: CGFloat(size),
            weight: fontWeight,
            design: fontFamily == .mono ? .monospaced : .default
        )
    }

    private var fontWeight: Font.Weight {
        switch weight {
        case ..<200: .ultraLight
        case ..<300: .thin
        case ..<400: .light
        case ..<500: .regular
        case ..<600: .medium
        case ..<700: .semibold
        case ..<800: .bold
        case ..<900: .heavy
        default: .black
        }
    }
}

extension View {
    /// Font, tracking and line height from a shared text-style token.
    func textStyle(_ token: TextStyleToken) -> some View {
        font(token.font)
            .tracking(CGFloat(token.letterSpacing?.floatValue ?? 0))
            .lineSpacing(token.lineHeight.map { CGFloat($0.floatValue - token.size) } ?? 0)
    }
}
