import SwiftUI
import FrameZero

private let cardCount = 4
private let pulseMinOpacity = 0.4
private let pulseDuration = 0.9

/// Pulsing placeholder cards while the first page loads; mirrors composeApp/features/home
/// `ProductionsSkeleton`.
struct ProductionsSkeleton: View {
    @State private var dimmed = true

    var body: some View {
        VStack(spacing: CGFloat(DesignTokens.shared.spacing.space16)) {
            ForEach(0..<cardCount, id: \.self) { _ in
                SkeletonCard()
            }
        }
        .opacity(dimmed ? pulseMinOpacity : 1)
        .onAppear {
            withAnimation(.linear(duration: pulseDuration).repeatForever(autoreverses: true)) {
                dimmed = false
            }
        }
        .accessibilityHidden(true)
    }
}

private struct SkeletonCard: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                    SkeletonBar(width: 140, height: 14)
                    SkeletonBar(width: 80, height: 10)
                }
                Spacer()
                SkeletonBar(width: 72, height: 20)
            }
            .padding(.bottom, CGFloat(spacing.space16))
            HStack {
                SkeletonBar(width: 90, height: 10)
                Spacer()
                SkeletonBar(width: 30, height: 10)
            }
            .padding(.bottom, CGFloat(spacing.space8))
            SkeletonBar(width: nil, height: 6)
                .padding(.bottom, CGFloat(spacing.space16))
            HStack(spacing: CGFloat(spacing.space16)) {
                SkeletonBar(width: 64, height: 12)
                SkeletonBar(width: 64, height: 12)
            }
        }
        .padding(CGFloat(spacing.space16))
        .background(
            Color(argb: ColorTokens.forScheme(colorScheme).cardBackground),
            in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        )
    }
}

/// A `nil` width fills the row.
private struct SkeletonBar: View {
    let width: CGFloat?
    let height: CGFloat
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4))
            .fill(Color(argb: ColorTokens.forScheme(colorScheme).border))
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil)
    }
}

#Preview {
    ProductionsSkeleton().padding()
}
