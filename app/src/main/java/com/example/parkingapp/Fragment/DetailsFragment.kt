// ✅ Updated DetailsFragment.kt with Slot Grid View
package com.example.parkingapp.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.parkingapp.R
import com.example.parkingapp.databinding.FragmentDetailsBinding
import com.example.parkingapp.model.Slot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val slotList = mutableListOf<Slot>()

    private var floorKey: String = "floor1" // default fallback

    // Control flag for enabling interaction
    private var canReserve: Boolean = false
    private var selectedDate: Long = 0L

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

        canReserve = selectedDate > 0

        loadSlots()
    }

    private fun loadSlots() {
        database.child("slots").child(floorKey)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    slotList.clear()
                    for (slotSnapshot in snapshot.children) {
                        val slotId = slotSnapshot.key ?: continue
                        val status = slotSnapshot.child("status").getValue(String::class.java) ?: "Available"
                        slotList.add(Slot(slotId, status))
                    }
                    renderSlotBlocks()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load slots", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "Please select date and time first.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (slot.status == "Available") {
                        reserveSlot(slot.slotId)
                    } else {
                        Toast.makeText(requireContext(), "Slot ${slot.slotId} is not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        topSlots.forEach { slot ->
            binding.slotGridTop.addView(createSlotView(slot))
        }

        bottomSlots.forEach { slot ->
            binding.slotGridBottom.addView(createSlotView(slot))
        }
    }

    private fun reserveSlot(slotId: String) {
        val uid = auth.currentUser?.uid ?: return

        database.child("slots").child(floorKey).child(slotId).child("status").setValue("Reserved")
        database.child("slots").child(floorKey).child(slotId).child("reservedBy").setValue(uid)
        database.child("reservations").child(uid).child(slotId).setValue(floorKey)

        Toast.makeText(requireContext(), "Reserved $slotId", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
