package com.apps.travel_app.ui.components

import FaIcons
import com.apps.travel_app.ui.utils.sendPostRequest
import com.guru.fontawesomecomposelib.FaIconType

fun weatherChip(
    latitude: Double,
    longitude: Double,
    callback: (FaIconType?) -> Unit
) {

    var condition: String
    Thread {
        val request = "{\"lat\":${latitude},\"lng\":${longitude}}" // NON-NLS
        condition = sendPostRequest(request, action = "weather") ?: "" // NON-NLS
        callback(
            when (condition) {
                "clear sky" -> FaIcons.SunRegular
                "few clouds", "scattered clouds" -> FaIcons.CloudSun
                "broken clouds" -> FaIcons.Cloud
                "shower rain" -> FaIcons.CloudRain
                "rain" -> FaIcons.CloudSunRain
                "snow" -> FaIcons.Snowflake
                "mist" -> FaIcons.Smog
                "thunderstorm" -> FaIcons.CloudShowersHeavy
                else -> null
            }
        )

    }.start()


}
