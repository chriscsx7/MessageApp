package com.cddev.messageapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cddev.messageapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap

class Register : AppCompatActivity() {
    private lateinit var register_editText_username: EditText
    private lateinit var register_editText_email: EditText
    private lateinit var register_editText_password: EditText
    private lateinit var register_editText_confirm_password: EditText
    private lateinit var register_button_register: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar!!.title = "Registro"
        init()

        register_button_register.setOnClickListener {
            validarDatos()
        }
    }

    private fun init() {
        enableEdgeToEdge()
        register_editText_username = findViewById(R.id.register_editText_username)
        register_editText_email = findViewById(R.id.register_editText_email)
        register_editText_password = findViewById(R.id.register_editText_password)
        register_editText_confirm_password = findViewById(R.id.register_editText_password_confirm)
        register_button_register = findViewById(R.id.register_registerBtn)

        auth = FirebaseAuth.getInstance()
    }

    private fun validarDatos() {
        val username = register_editText_username.text.toString()
        val email = register_editText_email.text.toString()
        val password = register_editText_password.text.toString()
        val confirm_password = register_editText_confirm_password.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty()) {
            toast("Por favor, rellene todos los campos")
        } else if (username.isEmpty()) {
            toast("Por favor, ingrese un nombre de usuario")
        } else if (email.isEmpty()) {
            toast("Por favor, ingrese un correo electrónico")
        } else if (password.isEmpty()) {
            toast("Por favor, ingrese una contraseña")
        } else if (confirm_password.isEmpty()) {
            toast("Por favor, repita su contraseña")
        } else if (password != confirm_password) {
            toast("Las contraseñas no coinciden")
        } else {
            registrarUsuario(email, password)
        }
    }

    private fun registrarUsuario(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var uid: String = ""
                    uid = auth.currentUser!!.uid
                    reference = FirebaseDatabase.getInstance().reference.child("Usuarios").child(uid)

                    val hashMap = HashMap<String, Any>()
                    val h_username: String = register_editText_username.text.toString()
                    val h_email: String = register_editText_email.text.toString()

                    hashMap["id"] = uid
                    hashMap["username"] = h_username
                    hashMap["email"] = h_email
                    hashMap["imagen"] = ""
                    hashMap["buscar"] = h_username.lowercase()

                    reference.updateChildren(hashMap).addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            toast("Usuario registrado correctamente")
                            finish()
                        }
                    }.addOnFailureListener { e ->
                        toast("${e.message}")
                    }
                } else {
                    toast("Error al registrar el usuario")
                }
            }
            .addOnFailureListener(this) {
                toast("Error al registrar el usuario")
            }
    }

    private fun toast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}