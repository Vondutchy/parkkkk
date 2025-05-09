package com.example.parkingapp.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.parkingapp.R
import com.example.parkingapp.databinding.FragmentDetailsBinding
import com.example.parkingapp.model.Slot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val slotList = mutableListOf<Slot>()
    private var selectedSlotId: String? = null

    private var floorKey: String = "floor1"
    private var selectedDate: Long = 0L
    private var plateNumber: String? = null
    private var canReserve: Boolean = false
    private lateinit var dateKey: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        floorKey = arguments?.getString("floor") ?: "floor1"
        selectedDate = arguments?.getLong("selectedDate") ?: 0L
        dateKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(selectedDate))

        binding.slotGridTop.columnCount = 5
        binding.slotGridBottom.columnCount = 5

        fetchVehicleList()

        binding.reserveButton.setOnClickListener {
            val slotId = selectedSlotId ?: return@setOnClickListener

            if (plateNumber.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select a vehicle.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            reserveSlot(slotId)
        }
    }

    private fun fetchVehicleList() {
        val uid = auth.currentUser?.uid ?: return
        val vehicleRef = database.child("user").child(uid).child("vehicles")

        vehicleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vehiclePlates = mutableListOf<String>()
                for (vehicleSnapshot in snapshot.children) {
                    val plate = vehicleSnapshot.child("type").getValue(String::class.java)
                    if (!plate.isNullOrBlank()) {
                        vehiclePlates.add(plate)
                    }
                }

                if (vehiclePlates.isNotEmpty()) {
                    setupDropdown(vehiclePlates)
                    canReserve = true
                } else {
                    Toast.makeText(requireContext(), "No vehicles found under your profile", Toast.LENGTH_SHORT).show()
                }

                loadSlots()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch vehicle info", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDropdown(plates: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, plates)
        binding.vehicleDropdown.setAdapter(adapter)
        binding.vehicleDropdown.setOnItemClickListener { parent, _, position, _ ->
            plateNumber = parent.getItemAtPosition(position) as String
        }
        plateNumber = plates.firstOrNull()
    }

    private fun loadSlots() {
        val slotPath = database.child("slots").child(floorKey).child(dateKey)

        // Check if slots exist for this date
        slotPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Auto-create A1–A10 if this is the first time
                    for (i in 1..10) {
                        val slotId = "A$i"
                        val slotData = mapOf(
                            "status" to "Available",
                            "reservedBy" to "",
                            "plateNumber" to ""
                        )
                        slotPath.child(slotId).setValue(slotData)
                    }
                }

                // Load and listen for status updates
                slotPath.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        slotList.clear()
                        for (slotSnap in dataSnapshot.children) {
                            val slotId = slotSnap.key ?: continue
                            val status = slotSnap.child("status").getValue(String::class.java) ?: "Available"
                            slotList.add(Slot(slotId, status))
                        }
                        renderSlotBlocks()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load slots", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to check slot existence", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderSlotBlocks() {
        binding.slotGridTop.removeAllViews()
        binding.slotGridBottom.removeAllViews()

        val topSlots = slotList.take(5)
        val bottomSlots = slotList.drop(5)

        fun createSlotView(slot: Slot): TextView {
            val layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(12, 12, 12, 12)
            }

            return TextView(requireContext()).apply {
                text = slot.slotId
                gravity = Gravity.CENTER
                textSize = 16f
                setPadding(24, 24, 24, 24)
                this.layoutParams = layoutParams
                setBackgroundResource(
                    when (slot.status) {
                        "Reserved" -> R.drawable.yellow_dashed_border
                        "Occupied" -> R.drawable.red_dashed_border_filled
                        else -> R.drawable.green_dashed_border
                    }
                )
                setOnClickListener {
                    if (!canReserve) {
                        Toast.makeText(requireContext(), "Please select a vehicle.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (slot.status == "Available") {
                        selectedSlotId = slot.slotId
                        binding.slotNumberText.text = "Slot ${slot.slotId}"
                        binding.slotLocationText.text = "${floorKey.uppercase()} Parking"
                        binding.slotStatusText.text = "Available"
                        binding.slotStatusText.setTextColor(resources.getColor(R.color.green))

                        binding.dateTimeLabel.visibility = View.VISIBLE
                        binding.dateTimeInput.visibility = View.VISIBLE
                        binding.totalLabel.visibility = View.VISIBLE
                        binding.totalAmount.visibility = View.VISIBLE
                        binding.reserveButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(requireContext(), "Slot ${slot.slotId} is not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        topSlots.forEach { binding.slotGridTop.addView(createSlotView(it)) }
        bottomSlots.forEach { binding.slotGridBottom.addView(createSlotView(it)) }
    }

    private fun reserveSlot(slotId: String) {
        val uid = auth.currentUser?.uid ?: return
        val slotRef = database.child("slots").child(floorKey).child(dateKey).child(slotId)

        slotRef.child("status").setValue("Reserved")
        slotRef.child("reservedBy").setValue(uid)
        slotRef.child("plateNumber").setValue(plateNumber)

        Toast.makeText(requireContext(), "Reserved $slotId for $plateNumber", Toast.LENGTH_SHORT).show()
        loadSlots()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
