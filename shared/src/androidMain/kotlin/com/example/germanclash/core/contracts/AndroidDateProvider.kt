package com.example.germanclash.core.contracts

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidDateProvider : DateProvider {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    override fun todayKey(): String = formatter.format(Date())
}
