import SwiftUI
import FrameZero

/// Shown while the session resolves; mirrors composeApp `SplashContent`.
struct SplashView: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space24)) {
            Text("app_name")
                .textStyle(DesignTokens.shared.typography.displayMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
            ProgressView()
                .tint(Color(argb: colors.accent))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(argb: colors.background))
    }
}

#Preview {
    SplashView()
}
