import SwiftUI
import FrameZero

typealias SignInModel = ScreenModel<SignInState, any SignInIntent>

extension SignInModel {
    convenience init(graph: IosAppGraph) {
        let viewModel = graph.signInViewModel
        self.init(viewModel, state: viewModel.state, onIntent: SignInViewModel.onIntent)
    }
}

/// Mirrors composeApp/features/auth `SignInScreen`; accessibility identifiers = its `SignInTestTags`.
struct SignInView: View {
    let model: SignInModel
    let onCreateAccount: () -> Void
    @FocusState private var focus: Field?
    @Environment(\.colorScheme) private var colorScheme

    private enum Field {
        case email, password
    }

    var body: some View {
        let state = model.state
        let spacing = DesignTokens.shared.spacing
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    AuthLogoHeader()
                        .padding(.vertical, CGFloat(spacing.space24))
                    AuthHeader(title: "signin_title", subtitle: "signin_subtitle")
                        .padding(.bottom, CGFloat(spacing.space24))
                    LabeledField(label: "field_email") {
                        InputField(
                            placeholder: "placeholder_email",
                            text: Binding(
                                get: { model.state.email },
                                set: { model.onIntent(SignInIntentEmailChanged(email: $0)) }
                            ),
                            systemImage: "envelope",
                            accessibilityID: "sign-in:email"
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
                                set: { model.onIntent(SignInIntentPasswordChanged(password: $0)) }
                            ),
                            systemImage: "lock",
                            isSecure: true,
                            showsRevealToggle: true,
                            accessibilityID: "sign-in:password"
                        )
                        .textContentType(.password)
                        .submitLabel(.go)
                        .focused($focus, equals: .password)
                        .onSubmit(submit)
                    }
                    .padding(.bottom, CGFloat(spacing.space24))
                    CtaButton(title: "btn_sign_in", isLoading: state.isLoading, action: submit)
                        .accessibilityIdentifier("sign-in:submit")
                    if let error = state.error {
                        ErrorText(error.localized)
                            .accessibilityIdentifier("sign-in:error")
                            .padding(.top, CGFloat(spacing.space8))
                    }
                }
                .disabled(state.isLoading)
            }
            .scrollDismissesKeyboard(.interactively)
            AuthFooter(
                prompt: "signin_new_user",
                action: "btn_create_account",
                isEnabled: !state.isLoading,
                onTap: onCreateAccount
            )
            .padding(.vertical, CGFloat(spacing.space24))
        }
        .padding(.horizontal, CGFloat(spacing.space16))
        .background(Color(argb: ColorTokens.forScheme(colorScheme).background))
        .errorToast(state.errorToast?.localized) {
            model.onIntent(SignInIntentToastDismissed.shared)
        }
        .task { await model.observe() }
    }

    private func submit() {
        focus = nil
        model.onIntent(SignInIntentSubmit.shared)
    }
}
