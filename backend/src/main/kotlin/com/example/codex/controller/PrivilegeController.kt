package com.example.codex.controller

import com.example.codex.domain.Privilege
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/privileges")
class PrivilegeController(private val userService: UserService) {
    @GetMapping
    fun list(): List<Privilege> = userService.allPrivileges()
}
