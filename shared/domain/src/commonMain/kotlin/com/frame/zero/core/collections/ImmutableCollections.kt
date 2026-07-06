package com.frame.zero.core.collections

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

/** Maps each element and collects the result into an [ImmutableList]. */
inline fun <T, R> Iterable<T>.mapImmutable(transform: (T) -> R): ImmutableList<R> = map(transform).toImmutableList()

/** Maps each element and collects the result into an [ImmutableSet]. */
inline fun <T, R> Iterable<T>.mapImmutableSet(transform: (T) -> R): ImmutableSet<R> = map(transform).toImmutableSet()

/** Empty persistent list when null — the immutable analogue of stdlib `orEmpty()`. */
fun <T> ImmutableList<T>?.orEmpty(): ImmutableList<T> = this ?: persistentListOf()

/** Empty persistent set when null — the immutable analogue of stdlib `orEmpty()`. */
fun <T> ImmutableSet<T>?.orEmpty(): ImmutableSet<T> = this ?: persistentSetOf()
