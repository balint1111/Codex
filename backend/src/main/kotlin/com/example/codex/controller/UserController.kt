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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users and their privileges")
class UserController(private val userService: UserService) {

    @GetMapping
    @Operation(summary = "List all users", description = "Retrieve a list of all users with their privileges")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    ])
    fun list(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<UserResponse>> {
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
    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "User successfully created"),
        ApiResponse(responseCode = "400", description = "Invalid input data"),
        ApiResponse(responseCode = "409", description = "Username already exists")
    ])
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
