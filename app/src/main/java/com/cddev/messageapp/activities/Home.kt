package com.cddev.messageapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cddev.messageapp.R
import com.cddev.messageapp.adapter.UserAdapter
import com.cddev.messageapp.model.Message
import com.cddev.messageapp.model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class Home : AppCompatActivity() {

    private lateinit var recyclerViewUsersHome: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.title = "Home"

        recyclerViewUsersHome = findViewById(R.id.recyclerViewUsersHome)
        val fabAddChat: FloatingActionButton = findViewById(R.id.fabAddChat)

        // Configurar RecyclerView
        userAdapter = UserAdapter(userList) { user ->
            // Redirigir al chat con el usuario seleccionado
            openChatWith(user)
        }
        recyclerViewUsersHome.adapter = userAdapter
        recyclerViewUsersHome.layoutManager = LinearLayoutManager(this)

        saveFcmToken()

        // Cargar usuarios desde Firebase
        loadUsers()

        // Acción del botón flotante
        fabAddChat.setOnClickListener {
            // Abrir actividad para agregar chat
            startActivity(Intent(this, AddChatActivity::class.java))
            finish()
        }
    }

    // Cargar chats desde Firebase
    private fun loadUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseMessage = FirebaseDatabase.getInstance().reference.child("messages")

        databaseMessage.addListenerForSingleValueEvent(object : ValueEventListener {
            private var receiverId: String? = null
            private var senderId: String? = null
            private var lastMsg: String? = null
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        if (message != null && (message.receiverId == currentUserId || message.senderId == currentUserId)) {
                            receiverId = message.receiverId
                            senderId = message.senderId
                            lastMsg = message.text
                        }
                    }
                    if (receiverId != null && senderId != null) {
                        getUserFromMessage(Message(senderId = senderId!!, receiverId = receiverId!!), currentUserId)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Error al cargar mensajes recientes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Menú de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            R.id.menuAddChat -> {
                startActivity(Intent(this, AddChatActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openChatWith(user: User) {
        val chatId = generateChatId(user.id)
        val username = user.username

        // Verificar si el chatId y username no son vacíos
        if (chatId.isNotEmpty() && username.isNotEmpty()) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("username", username)
            Log.d("AddChatActivity", "Iniciando ChatActivity con chatId: $chatId y username: $username")
            startActivity(intent)
        } else {
            Log.e("AddChatActivity", "Error: chatId o username vacíos.")
            Toast.makeText(this, "Error al abrir el chat", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserFromMessage(message: Message, currentUserId: String) {
        val otherUserId = if (message.senderId == currentUserId) message.receiverId else message.senderId
        val databaseUser = FirebaseDatabase.getInstance().reference.child("Usuarios").child(otherUserId)
        databaseUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    // Verificar si el usuario ya está en la lista
                    if (userList.none { it.id == user.id }) {
                        userList.add(user)
                        userAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateChatId(userId: String): String {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUserId.isNotEmpty() && userId.isNotEmpty()) {
            return if (currentUserId < userId) "$currentUserId$userId" else "$userId$currentUserId"
        }
        return "" // Retornar cadena vacía si el usuario no está autenticado o el ID es vacío
    }

    fun saveFcmToken() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios/$userId")
                    databaseRef.child("fcmToken").setValue(token)
                        .addOnSuccessListener {
                            println("Token FCM guardado exitosamente.")
                        }
                        .addOnFailureListener { e ->
                            println("Error al guardar el token FCM: ${e.message}")
                        }
                } else {
                    println("Error al obtener el token FCM: ${task.exception?.message}")
                }
            }
        }
    }
}
