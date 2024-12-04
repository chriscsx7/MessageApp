package com.cddev.messageapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cddev.messageapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class Login: AppCompatActivity() {
    private lateinit var login_editText_email: EditText
    private lateinit var login_editText_password: EditText
    private lateinit var login_loginBtn: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "Login"
        init()
        login_loginBtn.setOnClickListener {
            validarDatos()
        }
    }

    private fun init() {
        login_editText_email = findViewById(R.id.login_editText_email)
        login_editText_password = findViewById(R.id.login_editText_password)
        login_loginBtn = findViewById(R.id.login_loginBtn)
        auth = FirebaseAuth.getInstance()
    }

    private fun validarDatos() {
        val email = login_editText_email.text.toString()
        val password = login_editText_password.text.toString()
        if (email.isEmpty()) {
            toast("Ingrese su correo electrónico")
        }
        if (password.isEmpty()) {
            toast("Ingrese su contraseña")
        }
        if (email.isNotEmpty() && password.isNotEmpty()) {
            loginUsuario(email, password)
        }
    }

    private fun loginUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@Login, Home::class.java)
                    toast("Inicio de sesión exitoso")
                    startActivity(intent)
                    finish()
                } else {
                    toast("Error al iniciar sesión")
                }
            }
            .addOnFailureListener { e ->
                toast("${e.message}")
            }
    }

    private fun toast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}