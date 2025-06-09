package com.example.codex.controller

import com.example.codex.domain.User
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(private val userService: UserService) {

    @GetMapping
    fun list(): List<User> = userService.allUsers()

    @PostMapping("/register")
    fun register(@RequestParam username: String, @RequestParam password: String) {
        userService.register(username, password)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        userService.delete(id)
    }

    @GetMapping("/me")
    fun me(principal: java.security.Principal): User? {
        return userService.findByUsername(principal.name)
    }

    @PostMapping("/{id}/privileges")
    fun updatePrivileges(@PathVariable id: Long, @RequestBody privilegeIds: List<Long>) {
        userService.updatePrivileges(id, privilegeIds)
    }
}
