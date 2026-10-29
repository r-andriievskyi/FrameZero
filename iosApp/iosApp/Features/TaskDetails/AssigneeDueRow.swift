import SwiftUI
import FrameZero

private let avatarSize: CGFloat = 36

/// Mirrors composeApp/features/task-details `AssigneeDueRow`.
struct AssigneeDueRow: View {
    let assignee: TaskMember?
    let dueDate: Kotlinx_datetimeLocalDate?
    let isDueToday: Bool
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                Text("task_details_assignee")
                    .textStyle(typography.labelLarge)
                    .foregroundStyle(Color(argb: colors.textMuted))
                if let assignee {
                    HStack(spacing: CGFloat(spacing.space8)) {
                        MemberAvatar(initials: assignee.initials, colorHex: assignee.avatarColorHex, size: avatarSize)
                        Text(assignee.name)
                            .textStyle(typography.titleSmall)
                            .foregroundStyle(Color(argb: colors.textPrimary))
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityElement(children: .combine)
            VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
                Text("task_details_due_date")
                    .textStyle(typography.caption)
                    .fontWeight(.bold)
                    .foregroundStyle(Color(argb: colors.textMuted))
                if let dueDate {
                    Group {
                        if isDueToday {
                            Text("task_details_today")
                                .foregroundStyle(Color(argb: colors.errorText))
                        } else {
                            Text(dueDate.formatMedium())
                                .foregroundStyle(Color(argb: colors.textPrimary))
                        }
                    }
                    .textStyle(typography.titleSmall)
                }
            }
            .accessibilityElement(children: .combine)
        }
    }
}
