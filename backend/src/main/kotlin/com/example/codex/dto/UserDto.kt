package com.example.codex.dto

data class UserDto(
    val id: Long,
    val username: String,
    val privileges: List<PrivilegeDto>,
    val deleted: Boolean = false
)