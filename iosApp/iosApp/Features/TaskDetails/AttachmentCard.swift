import SwiftUI
import FrameZero

private let fileIconContainerSize: CGFloat = 44

/// Mirrors composeApp/features/task-details `AttachmentCard`. Tapping downloads the file; the shared
/// `AttachmentFileManager` then presents the system "Open in" menu.
struct AttachmentCard: View {
    let attachment: TaskDetailsAttachment
    let isDownloading: Bool
    let errorMessage: String?
    let onTap: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        let typography = DesignTokens.shared.typography
        let radius = DesignTokens.shared.radius
        VStack(alignment: .leading, spacing: CGFloat(spacing.space8)) {
            Text("task_details_attachment")
                .textStyle(typography.labelLarge)
                .foregroundStyle(Color(argb: colors.textMuted))
            Button(action: onTap) {
                HStack(spacing: CGFloat(spacing.space12)) {
                    Image(systemName: "doc")
                        .foregroundStyle(Color(argb: colors.accent))
                        .frame(width: fileIconContainerSize, height: fileIconContainerSize)
                        .background(
                            Color(argb: colors.accentSurface),
                            in: RoundedRectangle(cornerRadius: CGFloat(radius.radius8))
                        )
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: CGFloat(spacing.space2)) {
                        Text(attachment.fileName)
                            .textStyle(typography.titleSmall)
                            .foregroundStyle(Color(argb: colors.textPrimary))
                            .lineLimit(1)
                            .truncationMode(.tail)
                        Text(subtitle)
                            .textStyle(typography.bodySmall)
                            .foregroundStyle(Color(argb: subtitleColor(colors)))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(CGFloat(spacing.space12))
                .background(Color(argb: colors.inputBackground), in: RoundedRectangle(cornerRadius: CGFloat(radius.radius14)))
                .contentShape(RoundedRectangle(cornerRadius: CGFloat(radius.radius14)))
            }
            .buttonStyle(.plain)
        }
    }

    private var subtitle: String {
        if let errorMessage {
            return errorMessage
        }
        if isDownloading {
            return String(localized: "task_details_attachment_downloading")
        }
        return String(format: String(localized: "task_details_attachment_subtitle"), attachment.typeLabel, attachment.sizeLabel)
    }

    private func subtitleColor(_ colors: ColorTokens) -> Int64 {
        if errorMessage != nil {
            return colors.errorText
        }
        return isDownloading ? colors.accent : colors.textMuted
    }
}
