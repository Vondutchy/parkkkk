package com.example.parkingtangina

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingtangina.databinding.ActivityCreateAccountBinding
import com.example.parkingtangina.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CreateAccountActivity : AppCompatActivity() {

    private lateinit var name: String
    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var plateNumber: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val binding: ActivityCreateAccountBinding by lazy {
        ActivityCreateAccountBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //initialize Firebase auth
        auth = Firebase.auth
        //initialize Firebase auth
        database = Firebase.database.reference

        binding.signUpButton.setOnClickListener {

            name = binding.nameField.text.toString().trim()
            userName = binding.usernameField.text.toString().trim()
            email = binding.emailField.text.toString().trim()
            plateNumber = binding.plateNumberField.text.toString().trim()
            password = binding.passwordField.text.toString().trim()
            confirmPassword = binding.confirmPasswordField.text.toString().trim()

            if (name.isBlank() || userName.isBlank() || email.isBlank() || plateNumber.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Go to Sign In page
        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Go back to Main page
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account created Successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Account creation Failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure", task.exception)
            }
        }
    }

    private fun saveUserData() {
        name = binding.nameField.text.toString().trim()
        userName = binding.usernameField.text.toString().trim()
        email = binding.emailField.text.toString().trim()
        plateNumber = binding.plateNumberField.text.toString().trim()
        password = binding.passwordField.text.toString().trim()
        val user = UserModel(name, userName, email, plateNumber, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("user").child(userId).setValue(user)
    }
}

