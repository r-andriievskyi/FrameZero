import FrameZero

// iOS half of the shared error catalog (shared/ui_text `ErrorStringKey`). Android resolves the same
// keys in composeApp/shared/ui_text `UiTextExt.kt`; the copy lives in `Localizable.xcstrings` under
// the Android resource names. The exhaustive `switch` makes a new key a compile error here.

extension UiText {
    var localized: String {
        switch onEnum(of: self) {
        case .resource(let resource): resource.key.localized
        }
    }
}

extension ErrorStringKey {
    var localized: String {
        switch self {
        case .network: String(localized: "error_network")
        case .server: String(localized: "error_server")
        case .notFound: String(localized: "error_not_found")
        case .forbidden: String(localized: "error_forbidden")
        case .conflict: String(localized: "error_conflict")
        case .invalidCredentials: String(localized: "error_invalid_credentials")
        case .emailExists: String(localized: "error_email_exists")
        case .insufficientStorage: String(localized: "error_insufficient_storage")
        case .unknown: String(localized: "error_unknown")
        case .accountBiometricFailed: String(localized: "error_account_biometric_failed")
        case .authEmptyCredentials: String(localized: "error_auth_empty_credentials")
        case .authInvalidCredentials: String(localized: "error_auth_invalid_credentials")
        case .homeOffline: String(localized: "error_home_offline")
        case .createProductionTitleRequired: String(localized: "error_create_production_title_required")
        case .createProductionInvalidDates: String(localized: "error_create_production_invalid_dates")
        case .createProductionMissingDates: String(localized: "error_create_production_missing_dates")
        case .createProductionConflict: String(localized: "error_create_production_conflict")
        case .productionDetailsNotFound: String(localized: "error_production_details_not_found")
        case .productionDetailsForbidden: String(localized: "error_production_details_forbidden")
        case .productionDetailsConflict: String(localized: "error_production_details_conflict")
        case .createTaskTitleRequired: String(localized: "error_create_task_title_required")
        case .createTaskFileTooLarge: String(localized: "error_create_task_file_too_large")
        case .createTaskForbidden: String(localized: "error_create_task_forbidden")
        case .createTaskNotFound: String(localized: "error_create_task_not_found")
        case .createTaskConflict: String(localized: "error_create_task_conflict")
        case .taskDetailsForbidden: String(localized: "error_task_details_forbidden")
        case .taskDetailsNotFound: String(localized: "error_task_details_not_found")
        case .taskDetailsConflict: String(localized: "error_task_details_conflict")
        case .taskDetailsAttachmentOffline: String(localized: "error_task_details_attachment_offline")
        case .taskDetailsAttachmentStorage: String(localized: "error_task_details_attachment_storage")
        case .taskDetailsAttachmentGeneric: String(localized: "error_task_details_attachment_generic")
        }
    }
}
