package com.apps.travel_app.ui.components

import com.guru.fontawesomecomposelib.FaIconType

sealed class BottomBarItem(var route: String, var icon: FaIconType, var title: String) {
    object Home : BottomBarItem("home", FaIcons.Home, "Home")
    object Map : BottomBarItem("map",  FaIcons.GlobeEurope, "Map")
    object Trips : BottomBarItem("trips",  FaIcons.MapPin, "Trips")
    object Explore : BottomBarItem("explore",  FaIcons.MapRegular, "Explore")
    object Profile : BottomBarItem("profile",  FaIcons.UserAstronaut, "Profile")
}
