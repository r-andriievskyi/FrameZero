import SwiftUI
import FrameZero

// The two timeline cards; mirror composeApp/features/home `ScheduleEventCard` and `ScheduleTaskCard`.

/// An event: title, optional location, kind badge. Not tappable (no event screen on Android either).
struct ScheduleEventCard: View {
    let event: ScheduleEventUiModel
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let (surface, text, label): (Int64, Int64, LocalizedStringKey) = switch event.eventKind {
        case .shoot: (colors.accentSurface, colors.accentText, "event_kind_shoot")
        case .meeting: (colors.successSurface, colors.successText, "event_kind_meeting")
        case .review: (colors.warningSurface, colors.warningText, "event_kind_review")
        case .other: (colors.inputBackground, colors.textSecondary, "event_kind_other")
        }
        ScheduleCard {
            VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
                Text(event.title)
                    .textStyle(DesignTokens.shared.typography.titleSmall)
                    .foregroundStyle(Color(argb: colors.textPrimary))
                if let location = event.location {
                    Text(location)
                        .textStyle(DesignTokens.shared.typography.bodySmall)
                        .foregroundStyle(Color(argb: colors.textMuted))
                }
            }
        } badge: {
            ScheduleBadge(label: label, surface: surface, text: text)
        }
        .accessibilityElement(children: .combine)
    }
}

/// A task due on the selected day: title, due label, production, priority badge. Tap opens it.
struct ScheduleTaskCard: View {
    let task: ScheduleTaskUiModel
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let typography = DesignTokens.shared.typography
        let (surface, text, label): (Int64, Int64, LocalizedStringKey) = switch task.priority {
        case .high: (colors.priorityHighSurface, colors.priorityHighText, "schedule_priority_high")
        case .medium: (colors.priorityMedSurface, colors.priorityMedText, "schedule_priority_medium")
        case .low: (colors.priorityLowSurface, colors.priorityLowText, "schedule_priority_low")
        }
        let dueLabel: String = switch onEnum(of: task.dueLabel) {
        case .today: String(localized: "schedule_due_today")
        case .otherDate(let other): other.date.formatShort()
        }
        Button(action: onTap) {
            ScheduleCard {
                VStack(alignment: .leading, spacing: CGFloat(DesignTokens.shared.spacing.space4)) {
                    Text(task.title)
                        .textStyle(typography.titleMedium)
                        .foregroundStyle(Color(argb: colors.textPrimary))
                    Text(dueLabel)
                        .textStyle(typography.bodySmall)
                        .foregroundStyle(Color(argb: colors.textMuted))
                    Text(task.productionTitle)
                        .textStyle(typography.bodySmall)
                        .foregroundStyle(Color(argb: colors.textMuted))
                }
            } badge: {
                ScheduleBadge(label: label, surface: surface, text: text)
            }
        }
        .buttonStyle(.plain)
    }
}

/// Bordered card: leading content, trailing badge.
private struct ScheduleCard<Content: View, Badge: View>: View {
    @ViewBuilder let content: Content
    @ViewBuilder let badge: Badge
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius16))
        HStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
            content.frame(maxWidth: .infinity, alignment: .leading)
            badge
        }
        .padding(CGFloat(DesignTokens.shared.spacing.space16))
        .background(Color(argb: colors.cardBackground), in: shape)
        .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
        .contentShape(shape)
    }
}

private struct ScheduleBadge: View {
    let label: LocalizedStringKey
    let surface: Int64
    let text: Int64

    var body: some View {
        let spacing = DesignTokens.shared.spacing
        Text(label)
            .textStyle(DesignTokens.shared.typography.labelSmall)
            .foregroundStyle(Color(argb: text))
            .padding(.horizontal, CGFloat(spacing.space8))
            .padding(.vertical, CGFloat(spacing.space4))
            .background(Color(argb: surface), in: RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius4)))
    }
}
