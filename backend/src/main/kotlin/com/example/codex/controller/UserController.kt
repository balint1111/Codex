package com.example.codex.controller

import com.example.codex.dto.CreateUserRequest
import com.example.codex.dto.UserDto
import com.example.codex.mapper.UserMapper
import com.example.codex.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val userMapper: UserMapper
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping
    fun list(): ResponseEntity<List<UserDto>> {
        logger.info("Fetching all users")
        val users = userService.allUsers()
        return ResponseEntity.ok(userMapper.toDtoList(users))
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: Long): ResponseEntity<UserDto> {
        logger.info("Fetching user with id: {}", id)
        val user = userService.find(id)
        return if (user != null) {
            ResponseEntity.ok(userMapper.toDto(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<Void> {
        logger.info("Registering new user: {}", request.username)
        userService.register(request.username, request.password)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info("Deleting user with id: {}", id)
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun me(principal: java.security.Principal): ResponseEntity<UserDto> {
        logger.info("Fetching current user info for: {}", principal.name)
        val user = userService.findByUsername(principal.name)
        return if (user != null) {
            ResponseEntity.ok(userMapper.toDto(user))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/privileges")
    fun updatePrivileges(@PathVariable id: Long, @RequestBody privilegeIds: List<Long>): ResponseEntity<Void> {
        logger.info("Updating privileges for user id: {} with privileges: {}", id, privilegeIds)
        userService.updatePrivileges(id, privilegeIds)
        return ResponseEntity.noContent().build()
    }
}
