import SnapshotTesting
import SwiftUI
import XCTest
import FrameZero
@testable import iosApp

/// Laid-out width of component snapshots: a 390 pt iPhone screen minus the 16 pt side gutters.
private let componentWidth: CGFloat = 358
/// Tolerates anti-aliasing drift between simulator runtimes; a layout or color change still fails.
private let perceptualPrecision: Float = 0.98

extension XCTestCase {
    /// Records or compares `view` twice, light and dark, at component width and its natural height.
    /// The iOS counterpart of a Roborazzi `@LightDarkPreview` golden; references live in
    /// `__Snapshots__/<test file>/` beside the test. Recorded on the iPhone 17 Pro simulator (iOS 26.5), the
    /// device CI runs; a missing reference is recorded and fails the test once.
    ///
    /// Pass `drawHierarchy` for Liquid Glass: the default layer render drops glass entirely, while
    /// drawing the window hierarchy captures it (docs/ios-liquid-glass-plan.md, step 0).
    @MainActor
    func assertLightDarkSnapshots<V: View>(
        of view: V,
        drawHierarchy: Bool = false,
        file: StaticString = #filePath,
        testName: String = #function,
        line: UInt = #line
    ) {
        for (name, scheme, style) in [("light", ColorScheme.light, UIUserInterfaceStyle.light), ("dark", .dark, .dark)] {
            assertSnapshot(
                of: SnapshotSurface { view }.environment(\.colorScheme, scheme),
                as: .image(
                    drawHierarchyInKeyWindow: drawHierarchy,
                    perceptualPrecision: perceptualPrecision,
                    layout: .sizeThatFits,
                    traits: UITraitCollection(userInterfaceStyle: style)
                ),
                named: name,
                file: file,
                testName: testName,
                line: line
            )
        }
    }
}

/// The screen background behind a component, padded, at component width.
private struct SnapshotSurface<Content: View>: View {
    @ViewBuilder let content: Content
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        content
            .frame(width: componentWidth)
            .padding(CGFloat(DesignTokens.shared.spacing.space16))
            .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
    }
}
