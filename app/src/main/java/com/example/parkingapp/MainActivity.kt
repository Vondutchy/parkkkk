package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signUpBtn = findViewById<MaterialButton>(R.id.signUpBtn)
        val signInBtn = findViewById<MaterialButton>(R.id.signInBtn)
        val guestBtn = findViewById<MaterialButton>(R.id.guestBtn)

//        // ✅ THIS USES YOUR ORIGINAL STYLE — just cleaned
//        val db = FirebaseDatabase.getInstance().reference.child("slots")
//        val floorMap = mapOf(
//            "floor1" to "A",
//            "floor2" to "B",
//            "floor3" to "C",
//            "floor4" to "D"
//        )
//
//        for ((floorKey, prefix) in floorMap) {
//            for (i in 1..10) {
//                val slotId = "$prefix$i"
//                val slotRef = db.child(floorKey).child(slotId)
//                slotRef.child("status").setValue("Available")
//                slotRef.child("reservedBy").setValue("")
//                slotRef.child("plateNumber").setValue("")
//            }
//        }

        // 🚀 Navigation buttons
        signUpBtn.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        signInBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        guestBtn.setOnClickListener {
            startActivity(Intent(this, SignGuest::class.java))
        }
    }
}
