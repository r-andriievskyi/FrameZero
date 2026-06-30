package com.frame.zero.detekt

import org.jetbrains.kotlin.psi.KtElement

internal fun KtElement.fileName(): String = containingKtFile.name.substringAfterLast('/')

internal fun KtElement.filePath(): String = containingKtFile.virtualFilePath
