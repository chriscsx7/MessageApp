package com.cddev.messageapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cddev.messageapp.R
import com.cddev.messageapp.adapter.MessageAdapter
import com.cddev.messageapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var chatAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    private lateinit var chatId: String
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.title = "Chat"

        // Recuperar los datos pasados por el Intent
        chatId = intent.getStringExtra("chatId") ?: ""
        val username = intent.getStringExtra("username") ?: ""


        // Verificar si el chatId y username no son vacíos
        if (chatId.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Error: Datos de chat inválidos", Toast.LENGTH_SHORT).show()
            finish() // Cerrar la actividad si los datos son incorrectos
            return
        }

        // Mostrar el nombre de usuario en algún TextView o realizar alguna otra acción
        val usernameTextView: TextView = findViewById(R.id.textViewUserChat)
        usernameTextView.text = username

        // Inicialización de vistas
        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        // Configurar RecyclerView
        chatAdapter = MessageAdapter(messageList)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar la referencia a la base de datos de Firebase para el chat
        database = FirebaseDatabase.getInstance().reference.child("messages").child(chatId)

        // Escuchar mensajes en tiempo real
        listenForMessages()

        // Configurar botón de envío
        buttonSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val messageText = editTextMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        val message = Message(
            text = messageText,
            senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            timestamp = System.currentTimeMillis()
        )

        database.push().setValue(message).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                editTextMessage.text.clear()
                recyclerView.scrollToPosition(messageList.size - 1)
            } else {
                Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenForMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messageList.add(message)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
