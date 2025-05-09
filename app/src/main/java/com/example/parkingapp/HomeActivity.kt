package com.example.parkingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        navController = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)!!
            .findNavController()

        // Set default selected item to Home to match the default fragment
        bottomNav.selectedItemId = R.id.homeFragment

        bottomNav.setOnItemSelectedListener { item ->
            val uid = auth.currentUser?.uid

            // Always allow Home and Profile
            if (item.itemId == R.id.homeFragment || item.itemId == R.id.profileFragment) {
                navController.navigate(item.itemId)
                return@setOnItemSelectedListener true
            }

            // For other tabs, validate vehicle presence
            if (uid != null) {
                bottomNav.post {
                    database.child("user").child(uid).child("vehicles").get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists() && snapshot.childrenCount > 0) {
                                navController.navigate(item.itemId)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Please add a vehicle in your profile first.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(R.id.profileFragment)
                            }
                        }
                }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }

            true // Always return true to keep animation behavior
        }
    }
}
