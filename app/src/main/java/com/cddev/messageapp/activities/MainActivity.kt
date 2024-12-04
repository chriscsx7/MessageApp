package com.cddev.messageapp.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cddev.messageapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.messaging

class MainActivity: AppCompatActivity() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "messageApp"
    }
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("El token no se pudo obtener")
                return@addOnCompleteListener
            }
            val token = task.result
            // Guardar el token en la base de datos
            println("Token obtenido: $token")
        }
        init()
        createNotificationChannel()
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "MessageApp",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal de notificaciones de MessageApp"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}