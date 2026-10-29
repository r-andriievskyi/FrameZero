import SwiftUI
import FrameZero

private let logoContainerSize: CGFloat = 80
private let logoSize = logoContainerSize / 2
private let badgeSize: CGFloat = 36
private let badgeOverhang = badgeSize / 3

/// App mark in a card with a status badge on its corner; mirrors composeApp `UpdateLogoBadge`.
/// The mark is an SF Symbol until an iOS logo asset exists.
struct UpdateLogoBadge: View {
    let badgeSystemImage: String
    let badgeBackground: Color
    let badgeTint: Color
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        Image(systemName: "movieclapper")
            .resizable()
            .scaledToFit()
            .frame(width: logoSize, height: logoSize)
            .foregroundStyle(Color(argb: colors.textPrimary))
            .frame(width: logoContainerSize, height: logoContainerSize)
            .background(Color(argb: colors.cardBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            .padding([.trailing, .bottom], badgeOverhang)
            .overlay(alignment: .bottomTrailing) {
                Image(systemName: badgeSystemImage)
                    .foregroundStyle(badgeTint)
                    .frame(width: badgeSize, height: badgeSize)
                    .background(badgeBackground, in: Circle())
            }
            .accessibilityHidden(true)
    }
}

#Preview {
    UpdateLogoBadge(badgeSystemImage: "arrow.down", badgeBackground: .blue.opacity(0.2), badgeTint: .blue)
}
