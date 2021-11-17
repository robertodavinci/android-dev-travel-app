package com.apps.travel_app.models

import androidx.compose.ui.graphics.ImageBitmap
import java.util.*

class Destination {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var type: String = ""
    var country: Country = Country()
    var thumbnail: ImageBitmap? = null
    var name: String = ""
    var thumbnailUrl: String = ""
}

class Country {
    var name: String = ""
    var acronym: String = ""
    var currency: Currency = Currency.getInstance("EUR")
}