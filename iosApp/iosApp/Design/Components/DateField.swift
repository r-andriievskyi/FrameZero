import SwiftUI
import FrameZero

private let fieldHeight: CGFloat = 48

/// A dd.mm.yyyy field that opens a graphical calendar sheet (Cancel / OK); the iOS form of the
/// Material `DatePickerDialog` fields in task-create (`DueDateSection`) and production (`DateInputField`).
struct DateField: View {
    let date: Kotlinx_datetimeLocalDate?
    let placeholder: LocalizedStringKey
    /// Names the field for VoiceOver and the picker.
    let label: LocalizedStringKey
    var systemImage: String?
    var isEnabled = true
    let onChange: (Kotlinx_datetimeLocalDate) -> Void
    @State private var isPickerShown = false
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        let shape = RoundedRectangle(cornerRadius: CGFloat(DesignTokens.shared.radius.radius8))
        Button { isPickerShown = true } label: {
            HStack(spacing: CGFloat(DesignTokens.shared.spacing.space8)) {
                Group {
                    if let date {
                        Text(Self.format(date)).foregroundStyle(Color(argb: colors.textPrimary))
                    } else {
                        Text(placeholder).foregroundStyle(Color(argb: colors.textMuted))
                    }
                }
                .textStyle(DesignTokens.shared.typography.bodyLarge)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                if let systemImage {
                    Image(systemName: systemImage)
                        .foregroundStyle(Color(argb: colors.textMuted))
                        .accessibilityHidden(true)
                }
            }
            .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space16))
            .frame(minHeight: fieldHeight)
            .background(Color(argb: colors.inputBackground), in: shape)
            .overlay(shape.stroke(Color(argb: colors.border), lineWidth: CGFloat(DesignTokens.shared.border.hairline)))
            .contentShape(shape)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .accessibilityLabel(Text(label))
        .accessibilityValue(date.map { Text($0.formatMedium()) } ?? Text(placeholder))
        .gatedSheet(isPresented: isPickerShown, onDismiss: { isPickerShown = false }) {
            DatePickerSheet(label: label, initial: date?.date() ?? .now) { picked in
                onChange(Kotlinx_datetimeLocalDate(picked))
                isPickerShown = false
            } onCancel: {
                isPickerShown = false
            }
        }
    }

    /// dd.mm.yyyy, as both Android fields format it.
    private static func format(_ date: Kotlinx_datetimeLocalDate) -> String {
        String(format: "%02d.%02d.%d", date.day, Int32(date.month.ordinal) + 1, date.year)
    }
}

private struct DatePickerSheet: View {
    let label: LocalizedStringKey
    let onConfirm: (Date) -> Void
    let onCancel: () -> Void
    @State private var selection: Date
    @Environment(\.colorScheme) private var colorScheme

    init(label: LocalizedStringKey, initial: Date, onConfirm: @escaping (Date) -> Void, onCancel: @escaping () -> Void) {
        self.label = label
        _selection = State(initialValue: initial)
        self.onConfirm = onConfirm
        self.onCancel = onCancel
    }

    var body: some View {
        let colors = ColorTokens.forScheme(colorScheme)
        NavigationStack {
            DatePicker(label, selection: $selection, displayedComponents: .date)
                .datePickerStyle(.graphical)
                .tint(Color(argb: colors.accent))
                .padding(.horizontal, CGFloat(DesignTokens.shared.spacing.space16))
                .frame(maxHeight: .infinity, alignment: .top)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) { Button("date_picker_cancel", action: onCancel) }
                    ToolbarItem(placement: .confirmationAction) { Button("date_picker_ok") { onConfirm(selection) } }
                }
        }
        .presentationDetents([.medium, .large])
        .glassSheetBackground()
    }
}
