package com.example.parkingapp.Fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.parkingapp.R
import com.example.parkingapp.databinding.FragmentParkingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ParkingFragment : Fragment() {
    private var _binding: FragmentParkingBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedDate: Long = 0
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private var duration: Int = 1

    private var selectedPlate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            selectedDate = it.getLong("selectedDate", 0)
            startHour = it.getInt("startHour", 0)
            startMinute = it.getInt("startMinute", 0)
            endHour = it.getInt("endHour", 0)
            endMinute = it.getInt("endMinute", 0)
            duration = it.getInt("duration", 1)
        }

        showPlateSelectionDialog()
        loadParkingAvailability()
        setupClickListeners()
    }

    private fun showPlateSelectionDialog() {
        val uid = auth.currentUser?.uid ?: return
        val userRef = database.getReference("user").child(uid).child("vehicles")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plates = mutableListOf<String>()
                for (child in snapshot.children) {
                    val plate = child.child("plateNumber").getValue(String::class.java)
                    if (plate != null) plates.add(plate)
                }

                if (plates.isEmpty()) {
                    Toast.makeText(requireContext(), "No vehicles found. Please add one.", Toast.LENGTH_LONG).show()
                    return
                }

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Select Vehicle")

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, plates)
                builder.setAdapter(adapter) { dialog, which ->
                    selectedPlate = plates[which]
                    dialog.dismiss()
                }
                builder.setCancelable(false)
                builder.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch vehicles.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadParkingAvailability() {
        val parkingRef = database.getReference("parking")

        parkingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val floor1Available = getAvailableSlots(snapshot, "floor1")
                    val floor2Available = getAvailableSlots(snapshot, "floor2")
                    val floor3Available = getAvailableSlots(snapshot, "floor3")
                    val floor4Available = getAvailableSlots(snapshot, "floor4")

                    binding.firstFloorSlots.text = floor1Available.toString()
                    binding.secondFloorSlots.text = floor2Available.toString()
                    binding.thirdFloorSlots.text = floor3Available.toString()
                    binding.fourthFloorSlots.text = floor4Available.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load parking data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAvailableSlots(snapshot: DataSnapshot, floorKey: String): Int {
        if (!snapshot.hasChild(floorKey)) return 0
        return snapshot.child(floorKey).child("available").getValue(Int::class.java) ?: 0
    }

    private fun setupClickListeners() {
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
    }

    private fun navigateToFloorDetails(floorLabel: String) {
        val floorKey = when (floorLabel) {
            "1st Floor" -> "floor1"
            "2nd Floor" -> "floor2"
            "3rd Floor" -> "floor3"
            "4th Floor" -> "floor4"
            else -> "floor1"
        }

        val bundle = Bundle().apply {
            putString("floor", floorKey)
            putLong("selectedDate", selectedDate)
            putInt("startHour", startHour)
            putInt("startMinute", startMinute)
            putInt("endHour", endHour)
            putInt("endMinute", endMinute)
            putInt("duration", duration)
            putString("plateNumber", selectedPlate)
        }

        findNavController().navigate(R.id.detailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
