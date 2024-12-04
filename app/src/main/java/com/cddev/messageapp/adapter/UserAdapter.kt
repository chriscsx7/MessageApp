package com.cddev.messageapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cddev.messageapp.R
import com.cddev.messageapp.model.User

class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, onUserClick)
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val email: TextView = itemView.findViewById(R.id.textViewEmail)

        fun bind(user: User, onUserClick: (User) -> Unit) {
            userName.text = user.username
            email.text = user.email
            itemView.setOnClickListener {
                // Aquí se maneja el clic y se navega a la actividad de chat
                onUserClick(user)
            }
        }
    }
}
