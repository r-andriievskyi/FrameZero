import SwiftUI
import FrameZero

/// Stand-in for a destination whose SwiftUI screen has not landed yet. Back stays available
/// through the enclosing `NavigationStack`, so navigation can still be exercised.
struct NotMigratedView: View {
    let name: String
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            Text("Not migrated yet")
                .textStyle(typography.titleLarge)
                .foregroundStyle(Color(argb: colors.textPrimary))
            Text(name)
                .textStyle(typography.monoMedium)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: colors.background))
    }
}
