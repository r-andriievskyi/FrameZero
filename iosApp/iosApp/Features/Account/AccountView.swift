import SwiftUI
import FrameZero

typealias AccountModel = ScreenModel<AccountState, any AccountIntent, Never>

extension AccountModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.accountViewModel
        self.init(viewModel, state: viewModel.state, onIntent: AccountViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/account `AccountContent`; accessibility identifiers = its
/// `AccountTestTags`. Sign-out needs no navigation here: the session flips to logged-out and
/// `RootView` swaps to the auth flow.
struct AccountView: View {
    let model: AccountModel
    let onDeveloperOptions: () -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let state = model.state
        let colors = ColorTokens.forScheme(colorScheme)
        let spacing = DesignTokens.shared.spacing
        ScrollView {
            VStack(spacing: CGFloat(spacing.space24)) {
                SettingsSection(title: "section_account") {
                    // Profile, email, password and notifications have no destination yet — no-ops on
                    // Android too.
                    if let userName = state.userName {
                        SettingsRow(systemImage: "person", title: "settings_edit_profile", subtitle: userName) {}
                            .accessibilityIdentifier("account:edit-profile")
                        SettingsDivider()
                    }
                    if let email = state.email {
                        SettingsRow(systemImage: "envelope", title: "settings_email_address", subtitle: email) {}
                        SettingsDivider()
                    }
                    SettingsRow(
                        systemImage: "lock",
                        title: "settings_password_security",
                        subtitle: String(localized: "settings_password_last_changed")
                    ) {}
                }
                SettingsSection(title: "section_workspace") {
                    SettingsRow(
                        systemImage: "bell",
                        title: "settings_notifications",
                        subtitle: String(localized: "settings_notifications_subtitle")
                    ) {}
                }
                if state.appLockSupported {
                    SettingsSection(title: "section_security") {
                        SettingsToggleRow(
                            systemImage: "lock",
                            title: "settings_app_lock",
                            subtitle: "settings_app_lock_subtitle",
                            isOn: Binding(
                                // Only the ViewModel flips it: enabling waits for a successful
                                // biometric prompt, so a cancelled prompt snaps the switch back.
                                get: { model.state.appLockEnabled },
                                set: { model.onIntent(AccountIntentAppLockToggled(enabled: $0, prompt: appLockPrompt)) }
                            )
                        )
                        .accessibilityIdentifier("account:app-lock-toggle")
                    }
                }
                if state.developerOptionsEnabled {
                    SettingsSection(title: "section_developer") {
                        SettingsRow(
                            systemImage: "info.circle",
                            title: "settings_design_system",
                            subtitle: String(localized: "settings_design_system_subtitle"),
                            action: onDeveloperOptions
                        )
                    }
                }
                OutlinedCtaButton(title: "sign_out", color: Color(argb: colors.errorText)) {
                    model.onIntent(AccountIntentSignOutClicked.shared)
                }
                .accessibilityIdentifier("account:sign-out")
            }
            .padding(.horizontal, CGFloat(spacing.space16))
            .padding(.vertical, CGFloat(spacing.space16))
        }
        .background(Color(argb: colors.background))
        .navigationTitle("account_toolbar_title")
        .navigationBarTitleDisplayMode(.inline)
        .errorToast(state.appLockError?.localized) {
            model.onIntent(AccountIntentAppLockErrorDismissed.shared)
        }
        .task { await model.observe() }
    }

    private var appLockPrompt: BiometricPromptText {
        BiometricPromptText(
            title: String(localized: "app_lock_prompt_title"),
            subtitle: String(localized: "app_lock_prompt_subtitle"),
            negativeButton: String(localized: "app_lock_prompt_cancel")
        )
    }
}

private struct SettingsDivider: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Rectangle()
            .fill(Color(argb: ColorTokens.forScheme(colorScheme).border))
            .frame(height: CGFloat(DesignTokens.shared.border.hairline))
    }
}
