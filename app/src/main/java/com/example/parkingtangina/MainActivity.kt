package com.example.parkingtangina

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signUpBtn = findViewById<MaterialButton>(R.id.signUpBtn)
        val signInBtn = findViewById<MaterialButton>(R.id.signInBtn)
        val guestBtn = findViewById<MaterialButton>(R.id.guestBtn)

        signUpBtn.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        signInBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        guestBtn.setOnClickListener {
            startActivity(Intent(this, SignGuest::class.java))
        }
    }
}
