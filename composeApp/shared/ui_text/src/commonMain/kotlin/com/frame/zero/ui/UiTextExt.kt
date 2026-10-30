package com.frame.zero.ui

import androidx.compose.runtime.Composable
import framezero.composeapp.shared.ui_text.generated.resources.Res
import framezero.composeapp.shared.ui_text.generated.resources.error_account_biometric_failed
import framezero.composeapp.shared.ui_text.generated.resources.error_auth_empty_credentials
import framezero.composeapp.shared.ui_text.generated.resources.error_auth_invalid_credentials
import framezero.composeapp.shared.ui_text.generated.resources.error_conflict
import framezero.composeapp.shared.ui_text.generated.resources.error_create_production_conflict
import framezero.composeapp.shared.ui_text.generated.resources.error_create_production_invalid_dates
import framezero.composeapp.shared.ui_text.generated.resources.error_create_production_missing_dates
import framezero.composeapp.shared.ui_text.generated.resources.error_create_production_title_required
import framezero.composeapp.shared.ui_text.generated.resources.error_create_task_conflict
import framezero.composeapp.shared.ui_text.generated.resources.error_create_task_file_too_large
import framezero.composeapp.shared.ui_text.generated.resources.error_create_task_forbidden
import framezero.composeapp.shared.ui_text.generated.resources.error_create_task_not_found
import framezero.composeapp.shared.ui_text.generated.resources.error_create_task_title_required
import framezero.composeapp.shared.ui_text.generated.resources.error_email_exists
import framezero.composeapp.shared.ui_text.generated.resources.error_forbidden
import framezero.composeapp.shared.ui_text.generated.resources.error_home_offline
import framezero.composeapp.shared.ui_text.generated.resources.error_insufficient_storage
import framezero.composeapp.shared.ui_text.generated.resources.error_invalid_credentials
import framezero.composeapp.shared.ui_text.generated.resources.error_network
import framezero.composeapp.shared.ui_text.generated.resources.error_not_found
import framezero.composeapp.shared.ui_text.generated.resources.error_production_details_conflict
import framezero.composeapp.shared.ui_text.generated.resources.error_production_details_forbidden
import framezero.composeapp.shared.ui_text.generated.resources.error_production_details_not_found
import framezero.composeapp.shared.ui_text.generated.resources.error_server
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_attachment_generic
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_attachment_offline
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_attachment_storage
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_conflict
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_forbidden
import framezero.composeapp.shared.ui_text.generated.resources.error_task_details_not_found
import framezero.composeapp.shared.ui_text.generated.resources.error_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String =
  when (this) {
    is UiText.Resource -> stringResource(key.res)
  }

private val ErrorStringKey.res: StringResource
  get() = when (this) {
    ErrorStringKey.NETWORK -> Res.string.error_network
    ErrorStringKey.SERVER -> Res.string.error_server
    ErrorStringKey.NOT_FOUND -> Res.string.error_not_found
    ErrorStringKey.FORBIDDEN -> Res.string.error_forbidden
    ErrorStringKey.CONFLICT -> Res.string.error_conflict
    ErrorStringKey.INVALID_CREDENTIALS -> Res.string.error_invalid_credentials
    ErrorStringKey.EMAIL_EXISTS -> Res.string.error_email_exists
    ErrorStringKey.INSUFFICIENT_STORAGE -> Res.string.error_insufficient_storage
    ErrorStringKey.UNKNOWN -> Res.string.error_unknown
    ErrorStringKey.ACCOUNT_BIOMETRIC_FAILED -> Res.string.error_account_biometric_failed
    ErrorStringKey.AUTH_EMPTY_CREDENTIALS -> Res.string.error_auth_empty_credentials
    ErrorStringKey.AUTH_INVALID_CREDENTIALS -> Res.string.error_auth_invalid_credentials
    ErrorStringKey.HOME_OFFLINE -> Res.string.error_home_offline
    ErrorStringKey.CREATE_PRODUCTION_TITLE_REQUIRED -> Res.string.error_create_production_title_required
    ErrorStringKey.CREATE_PRODUCTION_INVALID_DATES -> Res.string.error_create_production_invalid_dates
    ErrorStringKey.CREATE_PRODUCTION_MISSING_DATES -> Res.string.error_create_production_missing_dates
    ErrorStringKey.CREATE_PRODUCTION_CONFLICT -> Res.string.error_create_production_conflict
    ErrorStringKey.PRODUCTION_DETAILS_NOT_FOUND -> Res.string.error_production_details_not_found
    ErrorStringKey.PRODUCTION_DETAILS_FORBIDDEN -> Res.string.error_production_details_forbidden
    ErrorStringKey.PRODUCTION_DETAILS_CONFLICT -> Res.string.error_production_details_conflict
    ErrorStringKey.CREATE_TASK_TITLE_REQUIRED -> Res.string.error_create_task_title_required
    ErrorStringKey.CREATE_TASK_FILE_TOO_LARGE -> Res.string.error_create_task_file_too_large
    ErrorStringKey.CREATE_TASK_FORBIDDEN -> Res.string.error_create_task_forbidden
    ErrorStringKey.CREATE_TASK_NOT_FOUND -> Res.string.error_create_task_not_found
    ErrorStringKey.CREATE_TASK_CONFLICT -> Res.string.error_create_task_conflict
    ErrorStringKey.TASK_DETAILS_FORBIDDEN -> Res.string.error_task_details_forbidden
    ErrorStringKey.TASK_DETAILS_NOT_FOUND -> Res.string.error_task_details_not_found
    ErrorStringKey.TASK_DETAILS_CONFLICT -> Res.string.error_task_details_conflict
    ErrorStringKey.TASK_DETAILS_ATTACHMENT_OFFLINE -> Res.string.error_task_details_attachment_offline
    ErrorStringKey.TASK_DETAILS_ATTACHMENT_STORAGE -> Res.string.error_task_details_attachment_storage
    ErrorStringKey.TASK_DETAILS_ATTACHMENT_GENERIC -> Res.string.error_task_details_attachment_generic
  }
