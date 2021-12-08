package com.apps.travel_app.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import java.util.*

open class Destination() : Parcelable {
    var id: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var type: String = ""
    var country: Country = Country()
    var thumbnail: ImageBitmap? = null
    var name: String = "Unknown"
    var thumbnailUrl: String = "https://render.fineartamerica.com/images/rendered/default/print/8.000/8.000/break/images-medium-5/lost-astronaut-roberta-ferreira.jpg"
    var rating: Float = 0f
    var description: String = ""
    var priceLevel: Float = 0f
    var isOpen: Boolean = false
    var address: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        type = parcel.readString() ?: ""
        name = parcel.readString() ?: ""
        thumbnailUrl = parcel.readString() ?: ""
        rating = parcel.readFloat()
        priceLevel = parcel.readFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isOpen = parcel.readBoolean()
        }
        description = parcel.readString() ?: ""
        address = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(type)
        parcel.writeString(name)
        parcel.writeString(thumbnailUrl)
        parcel.writeFloat(rating)
        parcel.writeFloat(priceLevel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(isOpen)
        }
        parcel.writeString(description)
        parcel.writeString(address)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Destination> {
        override fun createFromParcel(parcel: Parcel): Destination {
            return Destination(parcel)
        }

        override fun newArray(size: Int): Array<Destination?> {
            return arrayOfNulls(size)
        }
    }

}

class Country {
    var name: String = ""
    var acronym: String = ""
    var currency: Currency = Currency.getInstance("EUR")
}