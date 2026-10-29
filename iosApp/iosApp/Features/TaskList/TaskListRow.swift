import SwiftUI
import FrameZero

/// One task card: title, due date, status badge. Android's `TaskListItemCard` is still a stub;
/// this follows its TODO (title, dueDateLabel, status badge) in the dashboard's `MyTaskRow` style.
struct TaskListRow: View {
    let task: TaskListItemUi
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        Button(action: onTap) {
            HStack(spacing: CGFloat(spacing.space8)) {
                VStack(alignment: .leading, spacing: CGFloat(spacing.space4)) {
                    Text(task.title)
                        .textStyle(typography.bodyMedium)
                        .foregroundStyle(Color(argb: colors.textPrimary))
                    if let dueDateLabel = task.dueDateLabel {
                        Text(dueDateLabel)
                            .textStyle(typography.bodySmall)
                            .foregroundStyle(Color(argb: colors.textMuted))
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                TaskStatusBadge(status: task.status)
                Image(systemName: "chevron.right")
                    .foregroundStyle(Color(argb: colors.textPrimary))
                    .accessibilityHidden(true)
            }
            .padding(CGFloat(spacing.space16))
            .background(Color(argb: colors.cardBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            .contentShape(shape)
        }
        .buttonStyle(.plain)
    }
}

private struct TaskStatusBadge: View {
    let status: TaskStatus
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let (surface, text, label): (Int64, Int64, LocalizedStringKey) = switch status {
        case .open: (colors.accentSurface, colors.accentText, "task_list_status_open")
        case .done: (colors.successSurface, colors.successText, "task_list_status_done")
        }
        Text(label)
            .textStyle(DesignTokens.shared.typography.labelMedium)
            .fontWeight(.bold)
            .foregroundStyle(Color(argb: text))
            .padding(.horizontal, CGFloat(spacing.space8))
            .padding(.vertical, CGFloat(spacing.space4))
            .background(
                Color(argb: surface),
                in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
            )
    }
}
