package com.example.parkingtangina

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val signInButton = findViewById<MaterialButton>(R.id.signInButton)
        val signUpButton = findViewById<MaterialButton>(R.id.signUpButton)
        val backButton = findViewById<MaterialButton>(R.id.backButton)

        // Go to Sign In page
        signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Sign up logic placeholder
        signUpButton.setOnClickListener {
            // TODO: Implement sign-up logic
        }

        // Go back to Main page
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
