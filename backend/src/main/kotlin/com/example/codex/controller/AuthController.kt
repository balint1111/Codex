package com.example.codex.controller

import com.example.codex.service.KeycloakSessionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder

data class LoginRequest(
    val username: String,
    val password: String,
)

@RestController
class AuthController(
    private val keycloakSessionService: KeycloakSessionService,
) {
    @PostMapping("/api/auth/login")
    fun login(@RequestBody requestBody: LoginRequest, request: HttpServletRequest): ResponseEntity<Unit> {
        val authentication = keycloakSessionService.authenticate(requestBody.username, requestBody.password)
        SecurityContextHolder.getContext().authentication = authentication
        request.getSession(true)
        return ResponseEntity.ok().build()
    }
}
