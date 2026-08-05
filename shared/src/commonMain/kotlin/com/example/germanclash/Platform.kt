package com.example.germanclash

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform