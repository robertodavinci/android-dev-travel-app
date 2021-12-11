package com.apps.travel_app.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import java.util.*

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



}