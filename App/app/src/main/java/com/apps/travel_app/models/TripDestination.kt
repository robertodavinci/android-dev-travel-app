package com.apps.travel_app.models

import android.os.Parcel
import android.os.Parcelable
import com.guru.fontawesomecomposelib.FaIconType

class TripDestination : Destination {
    var kmToNextDestination: Float = 0f
    var minutesToNextDestination: Float = 0f
    var mediumToNextDestination: MediumType? = MediumType.Foot
    var hour: String = ""
    var minutes: Int = 0

    constructor()

    constructor(parcel: Parcel) : super(parcel) {
        kmToNextDestination = parcel.readFloat()
        minutesToNextDestination = parcel.readFloat()
        val i = parcel.readInt()
        mediumToNextDestination = if (i >= 0) MediumType.values()[i] else null
        hour = parcel.readString()!!
        minutes = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeFloat(kmToNextDestination)
        parcel.writeFloat(minutesToNextDestination)
        parcel.writeInt(mediumToNextDestination?.ordinal ?: -1)
        parcel.writeString(hour)
        parcel.writeInt(minutes)

    }

    companion object CREATOR : Parcelable.Creator<TripDestination> {
        override fun createFromParcel(parcel: Parcel): TripDestination {
            return TripDestination(parcel)
        }

        override fun newArray(size: Int): Array<TripDestination?> {
            return arrayOfNulls(size)
        }
    }
}

enum class MediumType {
    Car,
    Foot,
    Bike,
    Plane,
    Ferry,
    Bus,
    Tram,
    Train,
    Chairlift,
    Ski;

    companion object {

        fun mediumTypeToIcon(type: MediumType): FaIconType {

            return when (type) {
                Car -> FaIcons.Car
                Foot -> FaIcons.Walking
                Bike -> FaIcons.Biking
                Ferry -> FaIcons.Ship
                Plane -> FaIcons.Plane
                Bus -> FaIcons.Bus
                Tram -> FaIcons.Tram
                Train -> FaIcons.Train
                Chairlift -> FaIcons.Chair
                Ski -> FaIcons.Skiing
            }
        }
    }
}