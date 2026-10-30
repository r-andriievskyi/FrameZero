import FrameZero

// Presentation of the production wire enums; mirror `accentColorFor` and the `displayLabel()`
// helpers in composeApp/features/home `ProductionCard`. Reused by the production screens (C12/C13).

extension ProductionPhase {
    /// "PRE_PRODUCTION" → "Pre production", as Android derives it from the enum name.
    var displayLabel: String { enumDisplayLabel(name) }

    func accentColor(in colors: ColorTokens) -> Int64 {
        switch self {
        case .idea, .archived: colors.textMuted
        case .development: colors.developmentText
        case .financing: colors.warningText
        case .preProduction: colors.preProductionText
        case .production: colors.productionText
        case .postProduction: colors.postProductionText
        case .marketing: colors.accentText
        case .distribution: colors.distributionText
        case .theRelease: colors.successText // `RELEASE`, renamed by Obj-C export (`release` is reserved)
        }
    }
}

extension Genre {
    var displayLabel: String { enumDisplayLabel(name) }
}

private func enumDisplayLabel(_ name: String) -> String {
    let words = name.replacingOccurrences(of: "_", with: " ").lowercased()
    return words.prefix(1).uppercased() + words.dropFirst()
}
