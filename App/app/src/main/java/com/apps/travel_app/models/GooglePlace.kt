package com.apps.travel_app.models

class GooglePlace : Destination() {
    var reviews: List<Rating> = arrayListOf()
    var openingHours: List<OpeningHour> = arrayListOf()
    var phoneNumber: String = ""
}

class OpeningHour {
    var open: Hour = Hour()
    var close: Hour = Hour()
}

class Hour {
    var dayOfWeek: Int = 1
    var hour: String = ""
}