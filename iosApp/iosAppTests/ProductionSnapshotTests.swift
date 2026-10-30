import XCTest
import SwiftUI
import FrameZero
@testable import iosApp

/// Production list card and the production-details cards; data mirrors the composeApp previews.
@MainActor
final class ProductionSnapshotTests: XCTestCase {
    func testProductionCard() {
        assertLightDarkSnapshots(of: ProductionCard(
            production: ProductionUi(
                id: "p1",
                title: "Echoes of Silence",
                genre: .drama,
                phase: .production,
                progressPercent: 68,
                daysLeft: 24,
                membersCount: 12
            )
        ) {})
    }

    func testLoglineCard() {
        assertLightDarkSnapshots(of: LoglineCard(detail: Self.detail))
    }

    func testPipelineCard() {
        assertLightDarkSnapshots(of: PipelineCard(pipeline: Self.detail.pipeline, currentPhase: .production))
    }

    func testTeamCard() {
        assertLightDarkSnapshots(of: TeamCard(viewerCrew: Self.detail.viewerCrew!))
    }

    func testTasksCard() {
        assertLightDarkSnapshots(of: VStack(spacing: 16) {
            TasksCard(
                tasks: [
                    ProductionTaskUi(id: "1", title: "Lock shooting schedule", dueDateLabel: "Apr 12", isDone: false),
                    ProductionTaskUi(id: "2", title: "Send call sheets", dueDateLabel: nil, isDone: true),
                ],
                onAddTask: {}
            )
            TasksCard(tasks: [], onAddTask: {})
        })
    }

    func testPendingUploadBanner() {
        assertLightDarkSnapshots(of: VStack(spacing: 8) {
            PendingUploadBanner(
                upload: PendingUploadUi(uploadId: "1", taskTitle: "Lock the schedule", isFailed: false),
                onRetry: {},
                onDismiss: {}
            )
            PendingUploadBanner(
                upload: PendingUploadUi(uploadId: "1", taskTitle: "Lock the schedule", isFailed: true),
                onRetry: {},
                onDismiss: {}
            )
        })
    }

    private static let detail = ProductionDetailUi(
        title: "Echoes of Silence",
        logline: "A deaf composer rediscovers sound through the chaos of war.",
        phase: .production,
        progressPercent: 68,
        daysLeft: 24,
        membersCount: 12,
        budgetLabel: "$2,400,000",
        startDateLabel: "Feb 10, 2026",
        wrapDateLabel: "Aug 30, 2026",
        pipeline: [ProductionPhase.development, .preProduction, .production, .postProduction, .distribution]
            .enumerated()
            .map { index, phase in
                ProductionPipelinePhaseUi(phase: phase, label: phase.displayLabel, isCompleted: index < 2, isCurrent: index == 2)
            },
        viewerCrew: ViewerCrewUi(
            viewerRole: "Producer",
            manager: ProductionMemberUi(id: "m1", name: "Maya Rivera", role: "Director", initials: "MR", avatarColorHex: "#E91E63"),
            peers: [ProductionMemberUi(id: "m3", name: "Sara Lin", role: "DP", initials: "SL", avatarColorHex: "#9C27B0")],
            reports: [ProductionMemberUi(id: "m4", name: "Jake Morse", role: "1st AD", initials: "JM", avatarColorHex: "#009688")]
        )
    )
}
