import XCTest
import SwiftUI
@testable import iosApp

/// Design-system components; mirror the `@LightDarkPreview`s in composeApp/shared/design_system.
@MainActor
final class DesignComponentSnapshotTests: XCTestCase {
    func testCtaButtons() {
        assertLightDarkSnapshots(of: VStack(spacing: 12) {
            CtaButton(title: "Continue") {}
            CtaButton(title: "Continue", isLoading: true) {}
            OutlinedCtaButton(title: "Sign out", color: .red) {}
        })
    }

    func testToasts() {
        assertLightDarkSnapshots(of: VStack(spacing: 8) {
            AppToast(title: "Scene 12 saved to cloud", severity: .success)
            AppToast(
                title: "Call sheet not sent",
                severity: .warning,
                description: "Tomorrow's call sheet hasn't been distributed to crew. Send before 9pm.",
                actionLabel: "Send"
            ) {}
            AppToast(title: "Upload failed — check connection", severity: .error, actionLabel: "Retry") {}
        })
    }

    @available(iOS 26, *)
    func testGlass() {
        assertLightDarkSnapshots(of: GlassShowcase(), drawHierarchy: true)
    }
}
