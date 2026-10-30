import XCTest
import SwiftUI
import FrameZero
@testable import iosApp

/// Home chrome. The floating nav is Liquid Glass, so it's captured over real list content (glass over
/// a flat background would hide a regression to an opaque fill) via the window hierarchy.
@available(iOS 26, *)
@MainActor
final class HomeSnapshotTests: XCTestCase {
    func testFloatingBottomNav() {
        let cards = VStack(spacing: CGFloat(DesignTokens.shared.spacing.space12)) {
            ForEach(Array([ProductionPhase.production, .preProduction].enumerated()), id: \.offset) { index, phase in
                ProductionCard(
                    production: ProductionUi(
                        id: "p\(index)",
                        title: "Echoes of Silence",
                        genre: .drama,
                        phase: phase,
                        progressPercent: 68,
                        daysLeft: 24,
                        membersCount: 12
                    )
                ) {}
            }
        }
        assertLightDarkSnapshots(
            of: cards.overlay(alignment: .bottom) { FloatingBottomNav(selection: .constant(.productions)) },
            drawHierarchy: true
        )
    }
}
