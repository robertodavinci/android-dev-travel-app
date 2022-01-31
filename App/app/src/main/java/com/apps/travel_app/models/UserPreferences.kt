package com.apps.travel_app.models

data class UserPreferences(
    val colourMode: Boolean?,
    val notifications: Boolean?,
    val realName: String?,
    val realSurname: String?
)