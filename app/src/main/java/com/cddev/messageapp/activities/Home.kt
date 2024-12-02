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
import com.cddev.messageapp.model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

        // Cargar usuarios desde Firebase
        loadUsers()

        // Acción del botón flotante
        fabAddChat.setOnClickListener {
            // Abrir actividad para agregar chat
            startActivity(Intent(this, AddChatActivity::class.java))
        }
    }

    // Cargar chats desde Firebase
    private fun loadUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference.child("Usuarios")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.id != currentUserId) {
                        userList.add(user)
                    }
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun generateChatId(userId: String): String {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUserId.isNotEmpty() && userId.isNotEmpty()) {
            return if (currentUserId < userId) "$currentUserId:$userId" else "$userId:$currentUserId"
        }
        return "" // Retornar cadena vacía si el usuario no está autenticado o el ID es vacío
    }
}
