package com.cddev.messageapp.model

data class User(
    var id: String = "",
    val username: String = "",
    val email: String = "",
    var fcmToken: String = ""
)
