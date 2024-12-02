package com.cddev.messageapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cddev.messageapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity: AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registrarButton)
        loginButton.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
        }
        registerButton.setOnClickListener {
            val intent = Intent(applicationContext, Register::class.java)
            startActivity(intent)
        }
    }

    private fun comprobarSesion() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val intent = Intent(this@MainActivity, Home::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        comprobarSesion()
        super.onStart()
    }
}