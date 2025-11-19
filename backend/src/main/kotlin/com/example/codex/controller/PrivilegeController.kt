package com.example.codex.controller

import com.example.codex.domain.Privilege
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/privileges")
@CrossOrigin(origins = ["*"])
class PrivilegeController(
    private val userService: UserService,
) {
    @GetMapping
    fun list(): Mono<List<Privilege>> =
        userService
            .allPrivileges()
            .collectList()
}
