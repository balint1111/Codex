package com.example.codex.controller

import com.example.codex.dto.UserRegistrationRequest
import com.example.codex.dto.UserResponse
import com.example.codex.dto.UpdatePrivilegesRequest
import com.example.codex.dto.PrivilegeResponse
import com.example.codex.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import java.security.Principal

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun list(): ResponseEntity<List<UserResponse>> {
        val users = userService.allUsers()
        return ResponseEntity.ok(users.map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.find(id)
        return if (user != null) {
            ResponseEntity.ok(user.toResponse())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: UserRegistrationRequest): ResponseEntity<Void> {
        userService.register(request.username, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun me(principal: Principal): ResponseEntity<UserResponse> {
        val user = userService.findByUsername(principal.name)
        return if (user != null) {
            ResponseEntity.ok(user.toResponse())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/privileges")
    fun updatePrivileges(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePrivilegesRequest
    ): ResponseEntity<Void> {
        userService.updatePrivileges(id, request.privilegeIds)
        return ResponseEntity.noContent().build()
    }
}
