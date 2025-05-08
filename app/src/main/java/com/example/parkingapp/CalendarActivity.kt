package com.example.parkingapp

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingapp.databinding.FragmentCalendarBinding
import com.example.parkingapp.model.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate: Calendar? = null
    private var selectedStartHour = 7
    private var selectedStartMinute = 0
    private var selectedEndHour = 12
    private var selectedEndMinute = 0
    private var selectedDuration = 1 // in hours

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()
        setupListeners()
    }

    private fun setupCalendar() {
        // Set current month title
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.currentMonthText.text = monthFormat.format(calendar.time).uppercase()

        // Setup calendar grid
        binding.calendarGrid.layoutManager = GridLayoutManager(this, 7)
        calendarAdapter = CalendarAdapter(getDaysInMonth(), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
        binding.calendarGrid.adapter = calendarAdapter
    }

    private fun getDaysInMonth(): List<Date> {
        val days = mutableListOf<Date>()

        // Clone the calendar to avoid modifying original
        val monthCalendar = calendar.clone() as Calendar

        // Set to first day of month
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

        // Get day of week for first day of month
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1

        // Add empty days for days before first day of month
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        // Fill the calendar grid (6 weeks x 7 days)
        for (i in 0 until 42) {
            days.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    private fun setupListeners() {
        // Back button
//        binding.backButton.setOnClickListener {
//            finish()
//        }

        // Duration selection
        binding.duration1Hour.setOnClickListener {
            selectedDuration = 1
            updateEndTime()
        }

        binding.duration2Hours.setOnClickListener {
            selectedDuration = 2
            updateEndTime()
        }

        binding.duration3Hours.setOnClickListener {
            selectedDuration = 3
            updateEndTime()
        }

        // Time selection
        binding.startTimeButton.setOnClickListener {
            showStartTimePicker()
        }

        binding.endTimeButton.setOnClickListener {
            showEndTimePicker()
        }

        // Continue button
        binding.continueButton.setOnClickListener {
            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val reservationId = UUID.randomUUID().toString()
            val status = "active"
            val plateNumber = "ABC123" // Replace with actual user input
            val parkingSpot = "1st Floor Slot No. 5" // Set based on actual selection

            val endCalendar = selectedDate!!.clone() as Calendar
            endCalendar.set(Calendar.HOUR_OF_DAY, selectedEndHour)
            endCalendar.set(Calendar.MINUTE, selectedEndMinute)
            val endTimeMillis = endCalendar.timeInMillis

            FirebaseRepository.saveReservation(
                reservationId,
                userId,
                status,
                plateNumber,
                parkingSpot,
                endTimeMillis
            )

            // Navigate to location selection
            val intent = Intent(this, LocationSelectionActivity::class.java)
            intent.putExtra("selectedDate", selectedDate!!.timeInMillis)
            intent.putExtra("startHour", selectedStartHour)
            intent.putExtra("startMinute", selectedStartMinute)
            intent.putExtra("endHour", selectedEndHour)
            intent.putExtra("endMinute", selectedEndMinute)
            intent.putExtra("duration", selectedDuration)
            startActivity(intent)
        }

        // Bottom navigation
//        binding.homeNavButton.setOnClickListener {
//            val intent = Intent(this, HomeActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            startActivity(intent)
//        }
//
//        binding.parkingNavButton.setOnClickListener {
//            finish()
//        }
    }

    private fun showStartTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedStartHour = hourOfDay
                selectedStartMinute = minute
                updateStartTimeButton()
                updateEndTime()
            },
            selectedStartHour,
            selectedStartMinute,
            false
        )

        timePickerDialog.show()
    }

    private fun showEndTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedEndHour = hourOfDay
                selectedEndMinute = minute
                updateEndTimeButton()
            },
            selectedEndHour,
            selectedEndMinute,
            false
        )

        timePickerDialog.show()
    }

    private fun updateStartTimeButton() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedStartHour)
        calendar.set(Calendar.MINUTE, selectedStartMinute)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.startTimeButton.text = timeFormat.format(calendar.time)
    }

    private fun updateEndTimeButton() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedEndHour)
        calendar.set(Calendar.MINUTE, selectedEndMinute)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.endTimeButton.text = timeFormat.format(calendar.time)
    }

    private fun updateEndTime() {
        // Calculate end time based on start time and duration
        val endHour = selectedStartHour + selectedDuration
        val endMinute = selectedStartMinute

        // Handle overflow
        selectedEndHour = endHour % 24
        selectedEndMinute = endMinute

        updateEndTimeButton()
    }

    // Calendar Adapter
    inner class CalendarAdapter(
        private val days: List<Date>,
        private val currentMonth: Int,
        private val currentYear: Int
    ) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

        private var selectedPosition = -1

        inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dayText: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return CalendarViewHolder(view)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val date = days[position]
            val calendar = Calendar.getInstance()
            calendar.time = date

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            holder.dayText.text = day.toString()

            // Check if date is in current month
            val isSameMonth = calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.YEAR) == currentYear

            // Check if date is in past
            val isInPast = calendar.before(Calendar.getInstance())

            // Set text color and background based on conditions
            if (isSameMonth && !isInPast) {
                // Regular day in current month
                holder.dayText.setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.main_color))

                // Check if this date is selected
                if (position == selectedPosition) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(this@CalendarActivity, R.color.main_color))
                    holder.dayText.setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.white))
                } else {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(this@CalendarActivity, R.color.white))
                }

                // Set click listener for valid dates
                holder.itemView.setOnClickListener {
                    // Update selection
                    val oldPosition = selectedPosition
                    selectedPosition = position

                    // Update UI
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    // Update selected date
                    selectedDate = calendar.clone() as Calendar
                }
            } else if (isSameMonth && isInPast) {
                // Past date in current month - grey out
                holder.dayText.setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.medium_gray))
                holder.itemView.setBackgroundColor(ContextCompat.getColor(this@CalendarActivity, R.color.white))
                holder.itemView.setOnClickListener(null)
            } else {
                // Date in other month - invisible
                holder.dayText.setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.light_gray))
                holder.itemView.setBackgroundColor(ContextCompat.getColor(this@CalendarActivity, R.color.white))
                holder.itemView.setOnClickListener(null)
            }

            // Check if it's a Sunday
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                holder.dayText.setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.red))
            }
        }

        override fun getItemCount(): Int = days.size
    }
}