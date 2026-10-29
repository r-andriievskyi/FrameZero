import SwiftUI
import FrameZero

typealias RegisterModel = ScreenModel<RegisterState, any RegisterIntent>

extension RegisterModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.registerViewModel
        self.init(viewModel, state: viewModel.state, onIntent: RegisterViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/auth `RegisterScreen`; accessibility identifiers = its `RegisterTestTags`.
struct RegisterView: View {
    let model: RegisterModel
    let onSignIn: () -> Void
    @FocusState private var focus: Field?
    @Environment(\.colorScheme) private var colorScheme

    private enum Field {
        case firstName, lastName, email, password
    }

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    AuthLogoHeader()
                        .padding(.vertical, CGFloat(spacing.space24))
                    AuthHeader(title: "register_title", subtitle: "register_subtitle")
                        .padding(.bottom, CGFloat(spacing.space24))
                    LabeledField(label: "register_field_first_name") {
                        InputField(
                            placeholder: "register_placeholder_first_name",
                            text: Binding(
                                get: { model.state.firstName },
                                set: { model.onIntent(RegisterIntentFirstNameChanged(firstName: $0)) }
                            )
                        )
                        .textContentType(.givenName)
                        .submitLabel(.next)
                        .focused($focus, equals: .firstName)
                        .onSubmit { focus = .lastName }
                    }
                    .padding(.bottom, CGFloat(spacing.space16))
                    LabeledField(label: "register_field_last_name") {
                        InputField(
                            placeholder: "register_placeholder_last_name",
                            text: Binding(
                                get: { model.state.lastName },
                                set: { model.onIntent(RegisterIntentLastNameChanged(lastName: $0)) }
                            )
                        )
                        .textContentType(.familyName)
                        .submitLabel(.next)
                        .focused($focus, equals: .lastName)
                        .onSubmit { focus = .email }
                    }
                    .padding(.bottom, CGFloat(spacing.space16))
                    LabeledField(label: "field_email") {
                        InputField(
                            placeholder: "placeholder_email",
                            text: Binding(
                                get: { model.state.email },
                                set: { model.onIntent(RegisterIntentEmailChanged(email: $0)) }
                            )
                        )
                        .keyboardType(.emailAddress)
                        .textContentType(.username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .submitLabel(.next)
                        .focused($focus, equals: .email)
                        .onSubmit { focus = .password }
                    }
                    .padding(.bottom, CGFloat(spacing.space16))
                    LabeledField(label: "field_password") {
                        InputField(
                            placeholder: "placeholder_password",
                            text: Binding(
                                get: { model.state.password },
                                set: { model.onIntent(RegisterIntentPasswordChanged(password: $0)) }
                            ),
                            isSecure: true
                        )
                        .textContentType(.newPassword)
                        .submitLabel(.go)
                        .focused($focus, equals: .password)
                        .onSubmit(submit)
                    }
                    .padding(.bottom, CGFloat(spacing.space24))
                    CtaButton(title: "btn_create_account", isLoading: state.isLoading, action: submit)
                        .accessibilityIdentifier("register:submit")
                    if let error = state.error {
                        ErrorText(error.localized)
                            .accessibilityIdentifier("register:error")
                            .padding(.top, CGFloat(spacing.space8))
                    }
                }
                .disabled(state.isLoading)
            }
            .scrollDismissesKeyboard(.interactively)
            AuthFooter(
                prompt: "register_have_account",
                action: "btn_sign_in",
                isEnabled: !state.isLoading,
                onTap: onSignIn
            )
            .padding(.vertical, CGFloat(spacing.space24))
        }
        .padding(.horizontal, CGFloat(spacing.space16))
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .errorToast(state.errorToast?.localized) {
            model.onIntent(RegisterIntentToastDismissed.shared)
        }
        .task { await model.observe() }
    }

    private func submit() {
        focus = nil
        model.onIntent(RegisterIntentSubmit.shared)
    }
}
