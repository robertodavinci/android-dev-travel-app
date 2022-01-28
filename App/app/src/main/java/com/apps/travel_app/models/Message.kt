package com.apps.travel_app.models

class Message {
    var username: String = ""
    var body: String = ""
    var id: String = ""
    var time: Long = 0
    var userId: String = ""
    var messages: ArrayList<Message> = arrayListOf()
    var parent: String? = null
    var entityId: Int = 0
}