package com.example.codex.controller

import com.example.codex.dto.PrivilegeResponse
import com.example.codex.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/privileges")
class PrivilegeController(private val userService: UserService) {
    
    @GetMapping
    fun list(): ResponseEntity<List<PrivilegeResponse>> {
        val privileges = userService.allPrivileges()
        return ResponseEntity.ok(privileges.map { it.toResponse() })
    }
}
