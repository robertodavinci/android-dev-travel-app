package com.apps.travel_app.models

import androidx.compose.ui.graphics.ImageBitmap
import com.apps.travel_app.data.rooms.Location
import java.lang.Exception

open class Destination() {
    var id: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var type: String = ""
    var thumbnail: ImageBitmap? = null
    var name: String = ""
    var thumbnailUrl: String = "https://render.fineartamerica.com/images/rendered/default/print/8.000/8.000/break/images-medium-5/lost-astronaut-roberta-ferreira.jpg"
    var rating: Float = 0f
    var description: String = ""
    var priceLevel: Float = 0f
    var isOpen: Boolean = false
    var address: String = ""

    open fun fromLocation(location: Location) {
        id = location.lid.toString()
        latitude = location.latitude
        longitude = location.longitude
        type = location.type
        thumbnailUrl = location.thumbnailUrl
        rating = location.rating
        description = location.description
        address = location.address ?: ""
        name = location.name
    }

    fun toLocation(): Location {
        var lid = 0
        try {
            lid = id.toInt()
        } catch (e: Exception) {

        }
        return Location(
            lid = lid,
            latitude = latitude,
            longitude = longitude,
            type = type,
            thumbnailUrl = thumbnailUrl,
            rating = rating,
            description = description,
            address = address,
            name = name,
            phone_number = null
        )
    }

}