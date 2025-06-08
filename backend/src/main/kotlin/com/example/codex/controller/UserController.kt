package com.example.codex.controller

import com.example.codex.domain.User
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun list(): List<User> = userService.allUsers()

    @PostMapping
    fun register(@RequestParam username: String, @RequestParam password: String) {
        userService.register(username, password)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        userService.delete(id)
    }
}
