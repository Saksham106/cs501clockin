package com.example.cs501clockin.ui.navigation

object Routes {
    const val Home = "home"
    const val History = "history"
    const val Dashboard = "dashboard"
    const val Settings = "settings"

    const val EditSessionBase = "edit"
    const val EditSessionArg = "sessionId"
    const val EditSession = "$EditSessionBase/{$EditSessionArg}"

    fun editSession(sessionId: Long): String = "$EditSessionBase/$sessionId"
}

