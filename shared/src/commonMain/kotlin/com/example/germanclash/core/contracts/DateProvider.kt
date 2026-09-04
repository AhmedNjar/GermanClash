package com.example.germanclash.core.contracts

interface DateProvider {
    /** Returns a sortable, unique string for the current local date, e.g. "2023-10-27". */
    fun todayKey(): String
}
