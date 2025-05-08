package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.databinding.FragmentParkingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationSelectionActivity : AppCompatActivity() {
    private lateinit var binding: FragmentParkingBinding
    private val database = FirebaseDatabase.getInstance()

    // Booking details from previous screen
    private var selectedDate: Long = 0
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private var duration: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentParkingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get booking details from intent
        selectedDate = intent.getLongExtra("selectedDate", 0)
        startHour = intent.getIntExtra("startHour", 0)
        startMinute = intent.getIntExtra("startMinute", 0)
        endHour = intent.getIntExtra("endHour", 0)
        endMinute = intent.getIntExtra("endMinute", 0)
        duration = intent.getIntExtra("duration", 1)

        loadParkingAvailability()
        setupClickListeners()
    }

    private fun loadParkingAvailability() {
        val parkingRef = database.getReference("parking")

        parkingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get available slots for each floor
                    val floor1Available = getAvailableSlots(snapshot, "floor1")
                    val floor2Available = getAvailableSlots(snapshot, "floor2")
                    val floor3Available = getAvailableSlots(snapshot, "floor3")
                    val floor4Available = getAvailableSlots(snapshot, "floor4")

                    // Update UI
                    binding.firstFloorSlots.text = floor1Available.toString()
                    binding.secondFloorSlots.text = floor2Available.toString()
                    binding.thirdFloorSlots.text = floor3Available.toString()
                    binding.fourthFloorSlots.text = floor4Available.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun getAvailableSlots(snapshot: DataSnapshot, floorKey: String): Int {
        if (!snapshot.hasChild(floorKey)) return 5 // Default value

        val slotsAvailable = snapshot.child(floorKey).child("available")
            .getValue(Int::class.java) ?: 5

        return slotsAvailable
    }

    private fun setupClickListeners() {
        // Back button
//        binding.backButton.setOnClickListener {
//            finish()
//        }

        // Floor cards
        binding.firstFloorCard.setOnClickListener {
            navigateToFloorDetails("1st Floor")
        }

        binding.secondFloorCard.setOnClickListener {
            navigateToFloorDetails("2nd Floor")
        }

        binding.thirdFloorCard.setOnClickListener {
            navigateToFloorDetails("3rd Floor")
        }

        binding.fourthFloorCard.setOnClickListener {
            navigateToFloorDetails("4th Floor")
        }

        // Bottom navigation
//        binding.homeNavButton.setOnClickListener {
//            val intent = Intent(this, HomeActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            startActivity(intent)
//        }
//
//        binding.calendarNavButton.setOnClickListener {
//            finish() // Go back to calendar
//        }
    }

    private fun navigateToFloorDetails(floor: String) {
        val intent = Intent(this, FloorDetailsActivity::class.java)
        intent.putExtra("floor", floor)
        intent.putExtra("selectedDate", selectedDate)
        intent.putExtra("startHour", startHour)
        intent.putExtra("startMinute", startMinute)
        intent.putExtra("endHour", endHour)
        intent.putExtra("endMinute", endMinute)
        intent.putExtra("duration", duration)
        startActivity(intent)
    }
}