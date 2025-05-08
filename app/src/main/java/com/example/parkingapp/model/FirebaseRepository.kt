package com.example.parkingapp.model

import com.google.firebase.database.FirebaseDatabase

object FirebaseRepository {
    private val db = FirebaseDatabase.getInstance()

    fun saveReservation(
        reservationId: String,
        userId: String,
        status: String,
        plateNumber: String,
        parkingSpot: String,
        endTimeMillis: Long
    ) {
        val reservationData = mapOf(
            "userId" to userId,
            "status" to status,
            "plateNumber" to plateNumber,
            "parkingSpot" to parkingSpot,
            "endTime" to endTimeMillis
        )
        db.getReference("reservations")
            .child(reservationId)
            .setValue(reservationData)
    }

    fun saveUserName(userId: String, name: String) {
        db.getReference("user")
            .child(userId)
            .child("name")
            .setValue(name)
    }

    fun updateSlotAvailability(floor: String, available: Int) {
        db.getReference("parking/$floor/available").setValue(available)
    }
}
