import SwiftUI
import FrameZero

/// Mirrors composeApp/features/task-details `PriorityBadge`.
struct PriorityBadge: View {
    let priority: TaskDetailsPriority
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let (surface, text, label): (Int64, Int64, LocalizedStringKey) = switch priority {
        case .high: (colors.priorityHighSurface, colors.priorityHighText, "schedule_priority_high")
        case .medium: (colors.priorityMedSurface, colors.priorityMedText, "schedule_priority_medium")
        case .low: (colors.priorityLowSurface, colors.priorityLowText, "schedule_priority_low")
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

#Preview {
    HStack {
        PriorityBadge(priority: .high)
        PriorityBadge(priority: .medium)
        PriorityBadge(priority: .low)
    }
}
