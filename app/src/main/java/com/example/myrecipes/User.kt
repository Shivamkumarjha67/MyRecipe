package com.example.myrecipes

class User(
    val userId: String = "",
    val userEmail: String = "",
    val recipes: MutableList<String> = mutableListOf(),
    var userName: String = "",
    var Image: String = ""
)