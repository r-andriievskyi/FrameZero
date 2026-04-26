package com.frame.zero

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform