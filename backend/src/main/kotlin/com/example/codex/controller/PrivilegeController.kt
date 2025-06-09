package com.example.codex.controller

import com.example.codex.domain.Privilege
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/privileges")
@CrossOrigin(origins = ["*"])
class PrivilegeController(private val userService: UserService) {
    @GetMapping
    fun list(): List<Privilege> = userService.allPrivileges()
}
