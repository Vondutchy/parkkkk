package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var plateNumber: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var auth: FirebaseAuth

    private val binding: ActivityCreateAccountBinding by lazy {
        ActivityCreateAccountBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.signUpButton.setOnClickListener {
            userName = binding.usernameField.text.toString().trim()
            email = binding.emailField.text.toString().trim()
            plateNumber = binding.plateNumberField.text.toString().trim()
            password = binding.passwordField.text.toString().trim()
            confirmPassword = binding.confirmPasswordField.text.toString().trim()

            if (userName.isBlank() || email.isBlank() || plateNumber.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            } else if (userName.length < 4) {
                Toast.makeText(this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show()
            } else if (plateNumber.length < 6) {
                Toast.makeText(this, "Plate number must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }

        // Go to Sign In page
        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Go back to Main page
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Set display name as username
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build()

                FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)

                Toast.makeText(this, "Account created Successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }
}
