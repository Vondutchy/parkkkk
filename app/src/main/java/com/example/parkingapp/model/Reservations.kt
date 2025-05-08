package com.example.parkingapp.model

data class Reservation(
    var userId: String? = null,
    var status: String? = null,
    var plateNumber: String? = null,
    var parkingSpot: String? = null,
    var endTime: Long? = null
)
