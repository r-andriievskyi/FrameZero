package com.frame.zero.core.upload

import kotlinx.serialization.Serializable

/** Terminal budget for a pending upload — see [PendingUploadStore.recordFailure]. */
const val MAX_UPLOAD_ATTEMPTS = 4

@Serializable
enum class UploadFailureReason {
  /** A 4xx (other than 408/429) — the request itself is invalid, so retrying it unchanged
   *  can never succeed. Stop immediately rather than spend the retry budget. */
  Permanent,

  /** Network failure, 408/429, or 5xx — plausibly resolves on its own. */
  Transient
}

/**
 * Classifies an upload failure from the raw HTTP status. [DomainError] deliberately discards
 * this level of detail (see the error-handling audit), so the upload path reads the status off
 * the exception itself rather than going through the shared domain-error mapper.
 */
fun classifyUploadFailure(httpStatus: Int?): UploadFailureReason =
  when (httpStatus) {
    null -> UploadFailureReason.Transient
    in 400..499 if httpStatus != 408 && httpStatus != 429 -> UploadFailureReason.Permanent
    else -> UploadFailureReason.Transient
  }
