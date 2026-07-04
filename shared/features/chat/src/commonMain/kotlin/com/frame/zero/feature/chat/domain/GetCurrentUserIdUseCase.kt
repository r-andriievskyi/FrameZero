package com.frame.zero.feature.chat.domain

import com.frame.zero.core.session.UserCache

/**
 * The signed-in user's id, used to style own vs. other message bubbles. Read from the local
 * [UserCache] (populated at sign-in) rather than the network, so bubbles are styled correctly
 * even when the chat is opened offline.
 */
class GetCurrentUserIdUseCase(
  private val userCache: UserCache
) {
  operator fun invoke(): String? = userCache.load()?.id
}
