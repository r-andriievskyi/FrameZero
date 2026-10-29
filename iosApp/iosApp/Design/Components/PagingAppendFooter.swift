import SwiftUI
import FrameZero

/// Footer under a paged list: a spinner while the next page loads, message + retry when it fails.
/// Mirrors the append slot of design_system's `PagingLazyColumn` (`AppendErrorRetry`).
struct PagingAppendFooter: View {
    let append: any PageLoad
    let onRetry: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        switch onEnum(of: append) {
        case .loading:
            ProgressView()
                .tint(Color(argb: colors.accent))
                .padding(CGFloat(spacing.space16))
        case .failed(let failure):
            VStack(spacing: CGFloat(spacing.space8)) {
                Text(failure.message.localized)
                    .textStyle(DesignTokens.shared.typography.bodySmall)
                    .foregroundStyle(Color(argb: colors.textSecondary))
                    .multilineTextAlignment(.center)
                CtaButton(title: "action_retry", action: onRetry)
            }
            .padding(CGFloat(spacing.space16))
        case .idle:
            EmptyView()
        }
    }
}
