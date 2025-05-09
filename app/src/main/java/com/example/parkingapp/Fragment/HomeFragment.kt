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
        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun setupClickListeners() {
        binding.learnMoreButton.setOnClickListener {
            Toast.makeText(context, "Navigate to booking calendar", Toast.LENGTH_SHORT).show()
        }
        binding.firstFloorTile.setOnClickListener { navigateToFloorDetails("1st Floor") }
        binding.secondFloorTile.setOnClickListener { navigateToFloorDetails("2nd Floor") }
        binding.thirdFloorTile.setOnClickListener { navigateToFloorDetails("3rd Floor") }
        binding.fourthFloorTile.setOnClickListener { navigateToFloorDetails("4th Floor") }
        binding.reservationCard.setOnClickListener {
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
                        val contactNumber = snapshot.child("contactNumber").getValue(String::class.java)
                        binding.welcomeMessage.text = "Good Day, ${userName ?: "User"}!"
                        binding.contactNumberField.text = "Contact: ${contactNumber ?: "N/A"}"
                    } else {
                        binding.welcomeMessage.text = "Good Day, User!"
                        binding.contactNumberField.text = "Contact: N/A"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.welcomeMessage.text = "Good Day, User!"
                    binding.contactNumberField.text = "Contact: N/A"
                }
            })
        } else {
            binding.welcomeMessage.text = "Good Day, Guest!"
            binding.contactNumberField.text = "Contact: N/A"
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
                            val status = reservationSnapshot.child("status").getValue(String::class.java)
                            if (status == "active") {
                                activeReservationFound = true
                                val contactNumber = reservationSnapshot.child("contactNumber").getValue(String::class.java) ?: "Unknown"
                                val parkingSpot = reservationSnapshot.child("parkingSpot").getValue(String::class.java) ?: "Unknown"
                                val endTimeMillis = reservationSnapshot.child("endTime").getValue(Long::class.java)
                                binding.contactNumberField.text = "Contact: $contactNumber"
                                binding.parkingSpotText.text = parkingSpot
                                if (endTimeMillis != null) startCountdown(endTimeMillis)
                                else binding.timeRemaining.text = "Unknown"
                                binding.reservationCard.visibility = View.VISIBLE
                                break
                            }
                        }
                        //if (!activeReservationFound) binding.reservationCard.visibility = View.GONE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.reservationCard.visibility = View.GONE
                    }
                })
        } else {
            binding.reservationCard.visibility = View.GONE
        }
    }

    private fun startCountdown(endTimeMillis: Long) {
        timer?.cancel()
        val currentTimeMillis = System.currentTimeMillis()
        val timeRemainingMillis = endTimeMillis - currentTimeMillis
        if (timeRemainingMillis > 0) {
            timer = object : CountDownTimer(timeRemainingMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    binding.timeRemaining.text = String.format("%d:%02d HRS", hours, minutes)
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
                val floor1 = snapshot.child("floor1/available").getValue(Int::class.java) ?: 5
                val floor2 = snapshot.child("floor2/available").getValue(Int::class.java) ?: 5
                val floor3 = snapshot.child("floor3/available").getValue(Int::class.java) ?: 5
                val floor4 = snapshot.child("floor4/available").getValue(Int::class.java) ?: 5

                binding.firstFloorSlots.text = floor1.toString()
                binding.secondFloorSlots.text = floor2.toString()
                binding.thirdFloorSlots.text = floor3.toString()
                binding.fourthFloorSlots.text = floor4.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.firstFloorSlots.text = "5"
                binding.secondFloorSlots.text = "5"
                binding.thirdFloorSlots.text = "5"
                binding.fourthFloorSlots.text = "5"
            }
        })
    }

    private fun navigateToFloorDetails(floor: String) {
        Toast.makeText(context, "Navigate to $floor details", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
