package com.apps.travel_app.data.room.entity

import com.guru.fontawesomecomposelib.FaIconType

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