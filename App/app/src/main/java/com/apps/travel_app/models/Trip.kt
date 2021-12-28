package com.apps.travel_app.models

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
<<<<<<< Updated upstream
=======
import com.apps.travel_app.data.room.entity.Trip
import com.apps.travel_app.data.room.entity.TripAndDays
import com.apps.travel_app.data.room.entity.TripStep
>>>>>>> Stashed changes

class Trip() : Parcelable {
    var id: Int = 0
    var startingPoint: Destination = Destination()
    var days: Int = 1
        set (value) {
            field = if (value <= 0) 1 else value
        }
    var attributes: List<String> = arrayListOf()
    var creator: String = ""
    var thumbnail: ImageBitmap? = null
    var name: String = ""
    var thumbnailUrl: String = ""
    var rating: Float = 0f
    var destinations: ArrayList<TripDestination> = arrayListOf(
        TripDestination(),
        TripDestination(),
        TripDestination()
    )
    var description : String =""
    var season: String = ""
    var creationDate: String = ""
    var mine: Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        days = parcel.readInt()
        val attributesArray = ArrayList<String>()
        parcel.readList(attributesArray, String::class.java.classLoader)
        attributes = attributesArray
        creator = parcel.readString()!!
        name = parcel.readString()!!
        thumbnailUrl = parcel.readString()!!
        rating = parcel.readFloat()
        val mObjArray = ArrayList<TripDestination>()
        parcel.readTypedList(mObjArray, TripDestination.CREATOR)
        destinations = mObjArray
        startingPoint = parcel.readTypedObject(Destination.CREATOR) ?: Destination()
        season = parcel.readString()!!
        description = parcel.readString()!!
        creationDate = parcel.readString()!!
        mine = parcel.readInt() > 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(days)
        parcel.writeList(attributes)
        parcel.writeString(creator)
        parcel.writeString(name)
        parcel.writeString(thumbnailUrl)
        parcel.writeFloat(rating)
        parcel.writeTypedList(destinations)
        parcel.writeTypedObject(startingPoint, flags)
        parcel.writeString(season)
        parcel.writeString(description)
        parcel.writeString(creationDate)
        parcel.writeInt(if (mine) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Trip> {
        override fun createFromParcel(parcel: Parcel): Trip {
            return Trip(parcel)
        }

        override fun newArray(size: Int): Array<Trip?> {
            return arrayOfNulls(size)
        }
    }
}