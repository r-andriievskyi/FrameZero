import SwiftUI
import FrameZero

private let illustrationWidthFraction: CGFloat = 0.7
private let containerAspectRatio: CGFloat = 1.375
private let stackedCardWidthFraction: CGFloat = 0.82
private let stackedCardHeightFraction: CGFloat = 0.625
private let backCardOffsetFraction: CGFloat = 0.25
private let middleCardOffsetFraction: CGFloat = 0.156
private let frontCardWidthFraction: CGFloat = 0.9
private let frontCardHeightFraction: CGFloat = 0.44
private let illustrationBarHeight: CGFloat = 4
private let titleBarHeight: CGFloat = 8
private let subtitleBarHeight: CGFloat = 6

/// No productions yet: stacked-cards illustration, copy and a create button; mirrors
/// composeApp/features/home `EmptyState`.
struct ProductionsEmptyState: View {
    let onCreateProduction: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(spacing: 0) {
            EmptyStateIllustration()
                .containerRelativeFrame(.horizontal) { width, _ in width * illustrationWidthFraction }
                .aspectRatio(containerAspectRatio, contentMode: .fit)
                .padding(.bottom, CGFloat(spacing.space24))
            Text("projects_empty_title")
                .textStyle(typography.titleMedium)
                .foregroundStyle(Color(argb: colors.textPrimary))
                .padding(.bottom, CGFloat(spacing.space8))
            Text("projects_empty_description")
                .textStyle(typography.bodySmall)
                .foregroundStyle(Color(argb: colors.textMuted))
                .padding(.bottom, CGFloat(spacing.space24))
            CtaButton(title: "projects_create_button", action: onCreateProduction)
                .containerRelativeFrame(.horizontal) { width, _ in width * illustrationWidthFraction }
                .padding(.bottom, CGFloat(spacing.space16))
            Text("projects_empty_invite")
                .textStyle(typography.bodySmall)
                .foregroundStyle(Color(argb: colors.textMuted))
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.vertical, CGFloat(spacing.space24))
    }
}

private struct EmptyStateIllustration: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        GeometryReader { proxy in
            let width = proxy.size.width
            let height = proxy.size.height
            ZStack(alignment: .bottom) {
                IllustrationCard()
                    .frame(width: width * stackedCardWidthFraction, height: height * stackedCardHeightFraction)
                    .rotationEffect(.degrees(-5))
                    .offset(y: -height * backCardOffsetFraction)
                IllustrationCard()
                    .frame(width: width * stackedCardWidthFraction, height: height * stackedCardHeightFraction)
                    .overlay(alignment: .bottom) {
                        // Phase-colored progress strip, as on a real production card.
                        // Weights 1 : 1 : 0.5, like Android's `Modifier.weight`s.
                        let unit = (width * stackedCardWidthFraction - CGFloat(spacing.space8 * 2 + spacing.space4 * 2)) / 2.5
                        HStack(spacing: CGFloat(spacing.space4)) {
                            IllustrationBar(color: colors.warningText, height: illustrationBarHeight)
                                .frame(width: unit)
                            IllustrationBar(color: colors.successText, height: illustrationBarHeight)
                                .frame(width: unit)
                            IllustrationBar(color: colors.accent, height: illustrationBarHeight)
                                .frame(width: unit / 2)
                        }
                        .padding(CGFloat(spacing.space8))
                    }
                    .rotationEffect(.degrees(3))
                    .offset(y: -height * middleCardOffsetFraction)
                IllustrationCard()
                    .frame(width: width * frontCardWidthFraction, height: height * frontCardHeightFraction)
                    .overlay(alignment: .leading) {
                        VStack(alignment: .leading, spacing: CGFloat(spacing.space4)) {
                            IllustrationBar(color: colors.border, height: titleBarHeight)
                                .frame(width: width * frontCardWidthFraction * 0.4)
                            IllustrationBar(color: colors.border, height: subtitleBarHeight)
                                .frame(width: width * frontCardWidthFraction * 0.25)
                        }
                        .padding(CGFloat(spacing.space8))
                    }
            }
            .frame(width: width, height: height, alignment: .bottom)
        }
        .accessibilityHidden(true)
    }
}

private struct IllustrationCard: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        shape
            .fill(Color(argb: colors.cardBackground))
            .overlay(shape.stroke(Color(argb: colors.cardBorder), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
    }
}

private struct IllustrationBar: View {
    let color: Int64
    let height: CGFloat

    var body: some View {
        RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
            .fill(Color(argb: color))
            .frame(height: height)
    }
}

#Preview {
    ProductionsEmptyState {}.padding()
}
