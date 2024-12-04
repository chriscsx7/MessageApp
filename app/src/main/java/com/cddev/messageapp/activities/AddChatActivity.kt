package com.cddev.messageapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cddev.messageapp.R
import com.cddev.messageapp.adapter.UserAdapter
import com.cddev.messageapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)
        supportActionBar?.title = "Agregar Chat"

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(userList) { user ->
            openChatWith(user)
        }
        recyclerView.adapter = adapter

        // Obtener usuarios desde Firebase
        fetchUsersFromFirebase()
    }

    private fun fetchUsersFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference.child("Usuarios")

        // Agregar log para verificar si estamos obteniendo datos
        Log.d("AddChatActivity", "Iniciando la carga de usuarios desde Firebase")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                // Verificar si el snapshot tiene datos
                Log.d("AddChatActivity", "Datos obtenidos: ${snapshot.childrenCount} usuarios encontrados")

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.id != currentUserId) {
                        userList.add(user)
                    }
                }

                // Verificar si se agregaron usuarios a la lista
                Log.d("AddChatActivity", "Usuarios cargados: ${userList.size}")
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddChatActivity", "Error al cargar usuarios: ${error.message}")
                Toast.makeText(this@AddChatActivity, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
            return if (currentUserId < userId) "$currentUserId$userId" else "$userId$currentUserId"
        }
        return "" // Retornar cadena vacía si el usuario no está autenticado o el ID es vacío
    }

    override fun onBackPressed() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
    }
}

