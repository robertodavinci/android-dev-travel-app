package com.apps.travel_app.data.room.entity

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.travel_app.models.Country
import com.apps.travel_app.models.MediumType
import java.util.*

@Entity // we can optionall put (ignoredColumns = [""]) to avoid some columns
data class Country(
    //@PrimaryKey val name: String, // we can either do @AutoGenerate = true or get the one directly from Firebase
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "acronym") val acronym: Float?,
    @ColumnInfo(name = "currency") val currency: Currency // UREDI OVO

)