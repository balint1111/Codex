package com.example.codex.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val privileges: List<PrivilegeResponse>,
    val deleted: Boolean = false
)

data class PrivilegeResponse(
    val id: Long,
    val name: String
)

data class UpdatePrivilegesRequest(
    val privilegeIds: List<Long>
)