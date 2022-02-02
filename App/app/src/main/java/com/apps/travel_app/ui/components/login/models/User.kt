package com.apps.travel_app.ui.components.login.models
/**
 * User class, used for specific events in the app and for easier handling
 * of the local data.
 */
data class User(
    var email: String,
    var displayName: String,
    var id: String,
    var realName: String?,
    var realSurname: String?)