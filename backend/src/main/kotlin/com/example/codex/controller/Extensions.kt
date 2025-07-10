package com.example.codex.controller

import com.example.codex.domain.User
import com.example.codex.domain.Privilege
import com.example.codex.dto.UserResponse
import com.example.codex.dto.PrivilegeResponse

fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    username = this.username,
    privileges = this.privileges.map { it.toResponse() },
    deleted = this.deleted
)

fun Privilege.toResponse(): PrivilegeResponse = PrivilegeResponse(
    id = this.id,
    name = this.name
)