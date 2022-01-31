package com.apps.travel_app.ui.notifications

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