package com.example.codex.domain

data class User(
    val id: Long,
    val username: String,
    val password: String,
    val privileges: List<Privilege>,
    val deleted: Boolean = false
)
