package com.example.parkingapp.Fragment
import com.example.parkingapp.model.Vehicle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.parkingapp.R
import com.example.parkingapp.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import android.widget.EditText
import android.widget.TextView
import android.app.Activity
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class ProfileFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val dbRef = FirebaseDatabase.getInstance().reference

        val vehicleHeader = view.findViewById<LinearLayout>(R.id.vehicleDropdownHeader)
        val vehicleArrow = view.findViewById<ImageView>(R.id.vehicleDropdownArrow)
        val vehicleContainer = view.findViewById<LinearLayout>(R.id.vehicleDropdownContainer)
        val addVehicleBtn = view.findViewById<LinearLayout>(R.id.addVehicleBtn)
        var vehicleExpanded = false

        vehicleHeader.setOnClickListener {
            vehicleExpanded = !vehicleExpanded
            vehicleContainer.visibility = if (vehicleExpanded) View.VISIBLE else View.GONE
            vehicleArrow.setImageResource(
                if (vehicleExpanded) R.drawable.ic_arrow_drop_up else R.drawable.ic_arrow_drop_down
            )
        }

        val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedImageUri = uri
                    val profilePic = view.findViewById<ImageView>(R.id.profilePic)
                    profilePic.setImageURI(uri)
                }
            }
        }

        val profilePic = view.findViewById<ImageView>(R.id.profilePic)
        profilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        val notifHeader = view.findViewById<LinearLayout>(R.id.notificationsDropdownHeader)
        val notifArrow = view.findViewById<ImageView>(R.id.notificationDropdownArrow)
        val notifContainer = view.findViewById<LinearLayout>(R.id.notificationDropdownContainer)

        var notifExpanded = false

        notifHeader.setOnClickListener {
            notifExpanded = !notifExpanded
            notifContainer.visibility = if (notifExpanded) View.VISIBLE else View.GONE
            notifArrow.setImageResource(
                if (notifExpanded) R.drawable.ic_arrow_drop_up else R.drawable.ic_arrow_drop_down
            )
        }

        if (currentUser != null) {
            val notifUid = currentUser.uid
            dbRef.child("user").child(notifUid).child("notifications").get()
                .addOnSuccessListener { snapshot ->
                    notifContainer.removeAllViews()
                    snapshot.children.forEach { notifSnap ->
                        val notifKey = notifSnap.key
                        val message = notifSnap.getValue(String::class.java)
                        if (!message.isNullOrEmpty() && notifKey != null) {
                            val notifView = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                val messageText = TextView(requireContext()).apply {
                                    text = "• $message"
                                    textSize = 14f
                                    setPadding(16, 8, 16, 8)
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                }
                                val clearBtn = Button(requireContext()).apply {
                                    text = "X"
                                    textSize = 12f
                                    setOnClickListener {
                                        dbRef.child("user").child(notifUid).child("notifications").child(notifKey).removeValue()
                                        notifContainer.removeView(this@apply.parent as View)
                                    }
                                }
                                addView(messageText)
                                addView(clearBtn)
                            }
                            notifContainer.addView(notifView)
                        }
                    }
                }
        }

        val nameText = view.findViewById<TextView>(R.id.userName)
        val emailText = view.findViewById<TextView>(R.id.userEmail)
        val vehicleListContainer = view.findViewById<LinearLayout>(R.id.vehicleDropdownContainer)

        if (currentUser != null) {
            emailText.text = currentUser.email
            nameText.text = currentUser.displayName ?: "No name set"

            dbRef.child("user").child(currentUser.uid).child("vehicles").get()
                .addOnSuccessListener { snapshot ->
                    vehicleListContainer.removeAllViews()
                    vehicleListContainer.addView(addVehicleBtn)
                    snapshot.children.forEach { vehicleSnap ->
                        val vehicleId = vehicleSnap.key
                        val vehicle = vehicleSnap.getValue(Vehicle::class.java)
                        if (vehicle != null && vehicleId != null) {
                            val carLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                val carView = TextView(requireContext()).apply {
                                    text = "${vehicle.name}\n${vehicle.type}"
                                    textSize = 16f
                                    setPadding(16, 16, 16, 16)
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                }
                                val editBtn = Button(requireContext()).apply {
                                    text = "Edit"
                                    setOnClickListener {
                                        val editDialogView = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)
                                        val nameInput = editDialogView.findViewById<EditText>(R.id.vehicleNameField)
                                        val typeInput = editDialogView.findViewById<EditText>(R.id.vehicleTypeField)
                                        nameInput.setText(vehicle.name)
                                        typeInput.setText(vehicle.type)

                                        val saveEditBtn = editDialogView.findViewById<Button>(R.id.saveVehicleButton)
                                        val alert = android.app.AlertDialog.Builder(requireContext())
                                            .setView(editDialogView)
                                            .create()

                                        saveEditBtn.setOnClickListener {
                                            val newName = nameInput.text.toString().trim()
                                            val newType = typeInput.text.toString().trim()
                                            if (newName.isNotEmpty() && newType.isNotEmpty()) {
                                                val updatedVehicle = Vehicle(newName, newType)
                                                dbRef.child("user").child(currentUser.uid).child("vehicles").child(vehicleId)
                                                    .setValue(updatedVehicle)
                                                    .addOnSuccessListener {
                                                        alert.dismiss()
                                                        parentFragmentManager.beginTransaction().detach(this@ProfileFragment).attach(this@ProfileFragment).commit()
                                                    }
                                            }
                                        }

                                        alert.show()
                                    }
                                }
                                val deleteBtn = Button(requireContext()).apply {
                                    text = "X"
                                    setOnClickListener {
                                        dbRef.child("user").child(currentUser.uid).child("vehicles").child(vehicleId).removeValue()
                                        vehicleListContainer.removeView(this@apply.parent as View)
                                    }
                                }
                                addView(carView)
                                addView(editBtn)
                                addView(deleteBtn)
                            }
                            vehicleListContainer.addView(carLayout, vehicleListContainer.childCount - 1)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("FIREBASE", "❌ Failed to load vehicles", it)
                }
        }

        val logOutBtn = view.findViewById<Button>(R.id.logOutBtn)
        logOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val editProfileBtn = view.findViewById<Button>(R.id.editProfileButton)
        editProfileBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val editText = dialogView.findViewById<EditText>(R.id.editNameField)
            val saveBtn = dialogView.findViewById<Button>(R.id.saveNameButton)

            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            saveBtn.setOnClickListener {
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    nameText.text = newName
                    val user = FirebaseAuth.getInstance().currentUser
                    val updates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                    user?.updateProfile(updates)
                    alertDialog.dismiss()
                }
            }

            alertDialog.show()
        }

        addVehicleBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_vehicle, null)
            val nameInput = dialogView.findViewById<EditText>(R.id.vehicleNameField)
            val typeInput = dialogView.findViewById<EditText>(R.id.vehicleTypeField)
            val saveBtn = dialogView.findViewById<Button>(R.id.saveVehicleButton)

            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            saveBtn.setOnClickListener {
                val carName = nameInput.text.toString().trim()
                val carType = typeInput.text.toString().trim()

                if (carName.isNotEmpty() && carType.isNotEmpty()) {
                    val vehicle = Vehicle(carName, carType)
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val ref = FirebaseDatabase.getInstance().reference
                        val vehicleId = ref.child("user").child(uid).child("vehicles").push().key
                        if (vehicleId != null) {
                            ref.child("user").child(uid).child("vehicles").child(vehicleId)
                                .setValue(vehicle)
                                .addOnSuccessListener {
                                    Log.d("FIREBASE", "✅ Vehicle saved")
                                    parentFragmentManager.beginTransaction().detach(this@ProfileFragment).attach(this@ProfileFragment).commit()
                                }
                                .addOnFailureListener {
                                    Log.e("FIREBASE", "❌ Vehicle save failed", it)
                                }
                        }
                    }
                    alertDialog.dismiss()
                }
            }
            alertDialog.show()
        }

        return view
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
