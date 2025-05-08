package com.example.parkingapp.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.parkingapp.R
import com.example.parkingapp.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageSlider()
        setupClickListeners()
        loadUserData()
        loadActiveReservation()
        loadParkingAvailability()
    }

    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.home_background, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.home_background, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.home_background, ScaleTypes.FIT))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun setupClickListeners() {
        // Learn More button
        binding.learnMoreButton.setOnClickListener {
            // Navigate to calendar booking screen
            Toast.makeText(context, "Navigate to booking calendar", Toast.LENGTH_SHORT).show()
        }

        // Floor tiles click listeners
        binding.firstFloorTile.setOnClickListener {
            navigateToFloorDetails("1st Floor")
        }

        binding.secondFloorTile.setOnClickListener {
            navigateToFloorDetails("2nd Floor")
        }

        binding.thirdFloorTile.setOnClickListener {
            navigateToFloorDetails("3rd Floor")
        }

        binding.fourthFloorTile.setOnClickListener {
            navigateToFloorDetails("4th Floor")
        }

        // Current reservation card click listener
        binding.reservationCard.setOnClickListener {
            // Navigate to reservation details
            Toast.makeText(context, "View reservation details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("user").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("name").getValue(String::class.java)
                        binding.welcomeMessage.text = "Good Day, $userName!"
                    } else {
                        binding.welcomeMessage.text = "Good Day, User!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.welcomeMessage.text = "Good Day, User!"
                }
            })
        } else {
            binding.welcomeMessage.text = "Good Day, Guest!"
        }
    }

    private fun loadActiveReservation() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val reservationsRef = database.getReference("reservations")

            reservationsRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var activeReservationFound = false

                        for (reservationSnapshot in snapshot.children) {
                            val status = reservationSnapshot.child("status")
                                .getValue(String::class.java)

                            if (status == "active") {
                                activeReservationFound = true

                                // Get reservation details
                                val plateNumber = reservationSnapshot.child("plateNumber")
                                    .getValue(String::class.java) ?: "Unknown"
                                val parkingSpot = reservationSnapshot.child("parkingSpot")
                                    .getValue(String::class.java) ?: "Unknown"
                                val endTimeMillis = reservationSnapshot.child("endTime")
                                    .getValue(Long::class.java)

                                // Update UI
                                binding.plateNumberText.text = plateNumber
                                binding.parkingSpotText.text = parkingSpot

                                // Calculate time remaining if end time is available
                                if (endTimeMillis != null) {
                                    startCountdown(endTimeMillis)
                                } else {
                                    binding.timeRemaining.text = "Unknown"
                                }

                                // Show reservation card
                                binding.reservationCard.visibility = View.VISIBLE
                                break
                            }
                        }

                        if (!activeReservationFound) {
                            // No active reservation found, hide the card
                            binding.reservationCard.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.reservationCard.visibility = View.GONE
                    }
                })
        } else {
            // No user logged in, hide reservation card
            binding.reservationCard.visibility = View.GONE
        }
    }

    private fun startCountdown(endTimeMillis: Long) {
        // Cancel existing timer if any
        timer?.cancel()

        val currentTimeMillis = System.currentTimeMillis()
        val timeRemainingMillis = endTimeMillis - currentTimeMillis

        if (timeRemainingMillis > 0) {
            timer = object : CountDownTimer(timeRemainingMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60

                    val timeString = String.format("%d:%02d HRS", hours, minutes)
                    binding.timeRemaining.text = timeString
                }

                override fun onFinish() {
                    binding.reservationCard.visibility = View.GONE
                }
            }.start()
        } else {
            binding.reservationCard.visibility = View.GONE
        }
    }

    private fun loadParkingAvailability() {
        val parkingRef = database.getReference("parking")

        parkingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Default values in case data is missing
                    var floor1 = 5
                    var floor2 = 5
                    var floor3 = 5
                    var floor4 = 5

                    // Get available slots for each floor
                    if (snapshot.hasChild("floor1")) {
                        floor1 = snapshot.child("floor1").child("available")
                            .getValue(Int::class.java) ?: 5
                    }

                    if (snapshot.hasChild("floor2")) {
                        floor2 = snapshot.child("floor2").child("available")
                            .getValue(Int::class.java) ?: 5
                    }

                    if (snapshot.hasChild("floor3")) {
                        floor3 = snapshot.child("floor3").child("available")
                            .getValue(Int::class.java) ?: 5
                    }

                    if (snapshot.hasChild("floor4")) {
                        floor4 = snapshot.child("floor4").child("available")
                            .getValue(Int::class.java) ?: 5
                    }

                    // Update UI
                    binding.firstFloorSlots.text = floor1.toString()
                    binding.secondFloorSlots.text = floor2.toString()
                    binding.thirdFloorSlots.text = floor3.toString()
                    binding.fourthFloorSlots.text = floor4.toString()
                } else {
                    // No data available, show default values
                    binding.firstFloorSlots.text = "5"
                    binding.secondFloorSlots.text = "5"
                    binding.thirdFloorSlots.text = "5"
                    binding.fourthFloorSlots.text = "5"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Error loading data, show default values
                binding.firstFloorSlots.text = "5"
                binding.secondFloorSlots.text = "5"
                binding.thirdFloorSlots.text = "5"
                binding.fourthFloorSlots.text = "5"
            }
        })
    }

    private fun navigateToFloorDetails(floor: String) {
        // In a real app, this would navigate to a floor details screen
        // For now, just show a toast
        Toast.makeText(context, "Navigate to $floor details", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel timer to prevent memory leaks
        timer?.cancel()
    }
}