package com.apps.travel_app.ui.notifications

/**
 * Service used for notification handling. Checks whether the user has enabled
 * notifications in the settings and therefore allows them to receive notifications.
 */

import androidx.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationsService: FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = sharedPref.getBoolean("receiveNotification", false)
        if (enabled)
            super.onMessageReceived(p0)
    }
}