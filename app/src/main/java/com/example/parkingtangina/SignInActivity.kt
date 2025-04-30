package com.example.parkingtangina

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val signInConfirmBtn = findViewById<MaterialButton>(R.id.signInConfirmBtn)
        val backBtn = findViewById<MaterialButton>(R.id.backBtn)
        val createAccountBtn = findViewById<MaterialButton>(R.id.createAccountBtn)

        signInConfirmBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        createAccountBtn.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }
}
