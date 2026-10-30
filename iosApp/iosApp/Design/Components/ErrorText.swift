import SwiftUI
import FrameZero

/// Inline error copy; mirrors Compose's `ErrorText` (design_system).
struct ErrorText: View {
    let text: String
    @Environment(\.colorScheme) private var colorScheme

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .textStyle(DesignTokens.shared.typography.bodySmall)
            .foregroundStyle(Color(argb: ColorTokens.forScheme(colorScheme).errorText))
    }
}

#Preview {
    ErrorText("Title cannot be empty")
}
