package com.assem.runningtracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plot")
data class Plot(
    var lat1: Double,
    var long1: Double,
    var lat3: Double,
    var long3: Double,
    var lat2: Double,
    var long2: Double,
    var lat4: Double,
    var long4: Double,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}