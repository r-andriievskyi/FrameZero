import SwiftUI
import FrameZero

/// The task's participants as chips plus a "Manage" entry to the picker; mirrors
/// composeApp/features/task-details `ParticipantsSection`.
struct ParticipantsSection: View {
    let participants: [TaskDetailsMember]
    let onManage: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
            HStack {
                Text("task_details_participants")
                    .textStyle(typography.labelLarge)
                    .foregroundStyle(Color(argb: colors.textMuted))
                Spacer()
                Button(action: onManage) {
                    Text("task_details_participants_manage")
                        .textStyle(typography.labelLarge)
                        .foregroundStyle(Color(argb: colors.textSecondary))
                }
                .buttonStyle(.plain)
            }
            if participants.isEmpty {
                Text("task_details_participants_none")
                    .textStyle(typography.bodyMedium)
                    .foregroundStyle(Color(argb: colors.textMuted))
            } else {
                FlowLayout(spacing: CGFloat(spacing.space8)) {
                    ForEach(participants, id: \.userId) { member in
                        MemberChip(member: member)
                    }
                }
            }
        }
    }
}
