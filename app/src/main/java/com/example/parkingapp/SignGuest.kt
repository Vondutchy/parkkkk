package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SignGuest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest)

        val guestCreateAccountBtn = findViewById<MaterialButton>(R.id.guestCreateAccountBtn)
        val guestSignInBtn = findViewById<MaterialButton>(R.id.guestSignInBtn)
        val guestBackBtn = findViewById<MaterialButton>(R.id.guestBackBtn)

        guestCreateAccountBtn.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        guestSignInBtn.setOnClickListener {
            // TODO: Replace with actual sign-in logic later
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        guestBackBtn.setOnClickListener {
            finish() // closes current activity and returns to previous screen
        }
    }
}
