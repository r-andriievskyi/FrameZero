import SwiftUI
import FrameZero

/// One "My Tasks" card; mirrors `TaskCard` in composeApp/features/home `MyTasksSection`.
struct MyTaskRow: View {
    let task: DashboardTaskUi
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
                    HStack(spacing: CGFloat(spacing.space8)) {
                        Text(task.productionTitle)
                            .foregroundStyle(Color(argb: colors.textMuted))
                        if let dueLabel {
                            Text(dueLabel)
                                .foregroundStyle(Color(argb: dueColor(colors)))
                        }
                    }
                    .textStyle(typography.bodySmall)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
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

    private var dueLabel: String? {
        guard let dueDate = task.dueDate else { return nil }
        return switch task.dueUrgency {
        case .today: String(localized: "today")
        case .tomorrow: String(localized: "tomorrow")
        case .overdue, .normal: dueDate.formatShort()
        }
    }

    private func dueColor(_ colors: ColorTokens) -> Int64 {
        switch task.dueUrgency {
        case .overdue, .today: colors.errorText
        case .tomorrow: colors.warningText
        case .normal: colors.textMuted
        }
    }
}
