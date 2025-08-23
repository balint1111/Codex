package com.example.codex.controller

import com.example.codex.domain.User
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun list(): List<User> = userService.allUsers()

    @GetMapping("/{id}")
    fun find(
        @PathVariable id: Long,
    ): User? = userService.find(id)

    @PostMapping("/register")
    fun register(
        @RequestParam username: String,
        @RequestParam password: String,
    ) {
        userService.register(username, password)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ) {
        userService.delete(id)
    }

    @GetMapping("/me")
    fun me(principal: java.security.Principal): User? = userService.findByUsername(principal.name)

    @PutMapping("/me")
    fun updateMe(
        principal: java.security.Principal,
        @RequestBody req: ProfileUpdateRequest,
    ): User? = userService.updateProfile(principal.name, req.username, req.password)

    @PostMapping("/{id}/privileges")
    fun updatePrivileges(
        @PathVariable id: Long,
        @RequestBody privilegeIds: List<Long>,
    ) {
        userService.updatePrivileges(id, privilegeIds)
    }
}

data class ProfileUpdateRequest(
    val username: String,
    val password: String?,
)
