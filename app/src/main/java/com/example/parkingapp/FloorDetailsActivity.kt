package com.example.parkingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.databinding.FragmentDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class FloorDetailsActivity : AppCompatActivity() {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()

    // Booking details from previous screen
    private var selectedDate: Long = 0
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private var duration: Int = 1

    private var selectedSlot: String? = null
    private var floorName: String = "1st Floor"
    private val slotViews = mutableMapOf<String, View>()
    private val slotStatus = mutableMapOf<String, SlotStatus>()

    // Enum for slot states
    enum class SlotStatus {
        AVAILABLE,
        RESERVED,
        OCCUPIED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Get data from intent
        floorName = intent.getStringExtra("floor") ?: "1st Floor"
        selectedDate = intent.getLongExtra("selectedDate", 0)
        startHour = intent.getIntExtra("startHour", 0)
        startMinute = intent.getIntExtra("startMinute", 0)
        endHour = intent.getIntExtra("endHour", 0)
        endMinute = intent.getIntExtra("endMinute", 0)
        duration = intent.getIntExtra("duration", 1)

        binding.floorTitleText.text = floorName

        initializeSlotViews()
        loadParkingSlots()
        setupListeners()
    }

    private fun initializeSlotViews() {
        // Initialize mappings between slot IDs and views
        slotViews["A1"] = binding.slotA1
        slotViews["A2"] = binding.slotA2
        slotViews["A3"] = binding.slotA3
        // Add more slots as needed
    }

    private fun loadParkingSlots() {
        val floorRef = database.getReference("parking").child(floorName.replace(" ", "")
            .lowercase(Locale.ROOT))

        floorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val slotsSnapshot = snapshot.child("slots")

                    // Clear previous status
                    slotStatus.clear()

                    // Update status for each slot
                    for (slotSnapshot in slotsSnapshot.children) {
                        val slotId = slotSnapshot.key ?: continue

                        val isOccupied = slotSnapshot.child("isOccupied").getValue(Boolean::class.java) ?: false
                        val isReserved = slotSnapshot.child("isReserved").getValue(Boolean::class.java) ?: false

                        val status = when {
                            isOccupied -> SlotStatus.OCCUPIED
                            isReserved -> SlotStatus.RESERVED
                            else -> SlotStatus.AVAILABLE
                        }

                        slotStatus[slotId] = status

                        // Get user details if occupied
                        if (isOccupied) {
                            val userId = slotSnapshot.child("userId").getValue(String::class.java)
                            if (userId != null) {
                                // Fetch user's plate number and contact info
                                fetchUserDetails(userId, slotId)
                            }
                        } else {
                            // Update UI for available or reserved slots
                            updateSlotUI(slotId, status)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FloorDetailsActivity, "Failed to load parking data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserDetails(userId: String, slotId: String) {
        database.getReference("user").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val plateNumber = snapshot.child("plateNumber").getValue(String::class.java) ?: "Unknown"
                    val phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "No contact"

                    // Update the slot with user details
                    updateSlotUI(slotId, SlotStatus.OCCUPIED, plateNumber, phoneNumber)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateSlotUI(slotId: String, status: SlotStatus, plateNumber: String = "", phoneNumber: String = "") {
        // You'll need to implement this based on your actual view structure
        // This is just a placeholder to show the concept
        when (slotId) {
            "A1" -> {
                when (status) {
                    SlotStatus.OCCUPIED -> {
                        binding.plateNumberA1.text = plateNumber
                        binding.phoneNumberA1.text = phoneNumber
                        binding.carDetailsA1.visibility = View.VISIBLE
                        binding.slotA1Background.setBackgroundResource(R.drawable.red_dashed_border_filled)
                    }
                    SlotStatus.RESERVED -> {
                        binding.carDetailsA1.visibility = View.GONE
                        binding.slotA1Background.setBackgroundResource(R.drawable.yellow_dashed_border)
                    }
                    else -> {
                        binding.carDetailsA1.visibility = View.GONE
                        binding.slotA1Background.setBackgroundResource(R.drawable.green_dashed_border)
                    }
                }
            }

            "A2" -> {
                when (status) {
                    SlotStatus.OCCUPIED -> {
                        binding.plateNumberA2.text = plateNumber
                        binding.phoneNumberA2.text = phoneNumber
                        binding.carDetailsA2.visibility = View.VISIBLE
                        binding.slotA2Background.setBackgroundResource(R.drawable.red_dashed_border_filled)
                    }
                    SlotStatus.RESERVED -> {
                        binding.carDetailsA2.visibility = View.GONE
                        binding.slotA2Background.setBackgroundResource(R.drawable.yellow_dashed_border)
                    }
                    else -> {
                        binding.carDetailsA2.visibility = View.GONE
                        binding.slotA2Background.setBackgroundResource(R.drawable.green_dashed_border)
                    }
                }
            }

            "A3" -> {
                when (status) {
                    SlotStatus.OCCUPIED -> {
                        binding.plateNumberA3.text = plateNumber
                        binding.phoneNumberA3.text = phoneNumber
                        binding.carDetailsA3.visibility = View.VISIBLE
                        binding.slotA3Background.setBackgroundResource(R.drawable.red_dashed_border_filled)
                    }
                    SlotStatus.RESERVED -> {
                        binding.carDetailsA3.visibility = View.GONE
                        binding.slotA3Background.setBackgroundResource(R.drawable.yellow_dashed_border)
                    }
                    else -> {
                        binding.carDetailsA3.visibility = View.GONE
                        binding.slotA3Background.setBackgroundResource(R.drawable.green_dashed_border)
                    }
                }
            }
            // Add more slots similarly
        }
    }

    private fun setupListeners() {
        // Back button
//        binding.backButton.setOnClickListener {
//            finish()
//        }

        // Set up click listeners for slots
        binding.slotA1.setOnClickListener {
            selectSlot("A1")
        }

        binding.slotA2.setOnClickListener {
            selectSlot("A2")
        }

        binding.slotA3.setOnClickListener {
            selectSlot("A3")
        }

        // Reserve button
        binding.reserveButton.setOnClickListener {
            makeReservation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectSlot(slotId: String) {
        val status = slotStatus[slotId] ?: return

        // Update the slot info card
        binding.slotNumberText.text = "Slot $slotId"
        binding.slotLocationText.text = "$floorName Parking"

        when (status) {
            SlotStatus.AVAILABLE -> {
                // Slot is available for booking
                binding.slotStatusText.text = "Available"
                binding.slotStatusText.setTextColor(resources.getColor(R.color.green))

                // Show booking controls
                binding.dateTimeLabel.visibility = View.VISIBLE
                binding.dateTimeInput.visibility = View.VISIBLE
                binding.totalLabel.visibility = View.VISIBLE
                binding.totalAmount.visibility = View.VISIBLE
                binding.reserveButton.visibility = View.VISIBLE

                // Allow reservation
                selectedSlot = slotId

                // Update date/time from intent
                updateDateTimeDisplay()
                calculatePrice()
            }
            SlotStatus.RESERVED -> {
                // Slot is already reserved
                binding.slotStatusText.text = "Reserved"
                binding.slotStatusText.setTextColor(resources.getColor(R.color.yellow))

                // Hide booking controls
                binding.dateTimeLabel.visibility = View.GONE
                binding.dateTimeInput.visibility = View.GONE
                binding.totalLabel.visibility = View.GONE
                binding.totalAmount.visibility = View.GONE
                binding.reserveButton.visibility = View.GONE

                // Can't select this slot
                selectedSlot = null
            }
            SlotStatus.OCCUPIED -> {
                // Slot is occupied
                binding.slotStatusText.text = "Booked For The Day"
                binding.slotStatusText.setTextColor(resources.getColor(R.color.red))

                // Hide booking controls
                binding.dateTimeLabel.visibility = View.GONE
                binding.dateTimeInput.visibility = View.GONE
                binding.totalLabel.visibility = View.GONE
                binding.totalAmount.visibility = View.GONE
                binding.reserveButton.visibility = View.GONE

                // Can't select this slot
                selectedSlot = null
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDateTimeDisplay() {
        // Convert the stored date and time to display format
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(calendar.time)

        // Format start and end times
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour)
        startCalendar.set(Calendar.MINUTE, startMinute)

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour)
        endCalendar.set(Calendar.MINUTE, endMinute)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeStr = timeFormat.format(startCalendar.time)
        val endTimeStr = timeFormat.format(endCalendar.time)

        binding.dateTimeInput.setText("$dateStr | $startTimeStr - $endTimeStr")
    }

    @SuppressLint("SetTextI18n")
    private fun calculatePrice() {
        // Calculate price based on duration
        val pricePerHour = 20 // PHP per hour
        val totalPrice = duration * pricePerHour

        binding.totalAmount.text = "₱$totalPrice"
    }

    private fun makeReservation() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to make a reservation", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSlot == null) {
            Toast.makeText(this, "Please select a slot", Toast.LENGTH_SHORT).show()
            return
        }

        // Create start and end time calendar objects
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = selectedDate
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour)
        startCalendar.set(Calendar.MINUTE, startMinute)

        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = selectedDate
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour)
        endCalendar.set(Calendar.MINUTE, endMinute)

        // Get user details
        database.getReference("user").child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val plateNumber = snapshot.child("plateNumber").getValue(String::class.java) ?: "Unknown"

                    // Create reservation
                    val reservationId = database.getReference("reservations").push().key ?: return

                    val reservationMap = hashMapOf(
                        "userId" to currentUser.uid,
                        "slotId" to selectedSlot,
                        "floor" to floorName,
                        "startTime" to startCalendar.timeInMillis,
                        "endTime" to endCalendar.timeInMillis,
                        "status" to "active",
                        "plateNumber" to plateNumber,
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Save to database
                    database.getReference("reservations").child(reservationId).setValue(reservationMap)
                        .addOnSuccessListener {
                            // Update slot status
                            val floorKey = floorName.replace(" ", "").lowercase(Locale.ROOT)

                            // Mark slot as reserved
                            database.getReference("parking")
                                .child(floorKey)
                                .child("slots")
                                .child(selectedSlot!!)
                                .child("isReserved")
                                .setValue(true)
                                .addOnSuccessListener {
                                    // Update available count
                                    updateAvailableSlotsCount(floorKey)

                                    Toast.makeText(this@FloorDetailsActivity, "Reservation successful", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@FloorDetailsActivity, "Failed to update slot: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@FloorDetailsActivity, "Reservation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FloorDetailsActivity, "Failed to get user details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAvailableSlotsCount(floorKey: String) {
        // Get current count of available slots
        database.getReference("parking").child(floorKey).child("slots")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var availableCount = 0

                        for (slotSnapshot in snapshot.children) {
                            val isOccupied = slotSnapshot.child("isOccupied").getValue(Boolean::class.java) ?: false
                            val isReserved = slotSnapshot.child("isReserved").getValue(Boolean::class.java) ?: false

                            if (!isOccupied && !isReserved) {
                                availableCount++
                            }
                        }

                        // Update available count
                        database.getReference("parking").child(floorKey).child("available")
                            .setValue(availableCount)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
}