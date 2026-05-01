package com.example.codex.controller

import com.example.codex.domain.User
import com.example.codex.jooq.tables.pojos.UserPrivilege
import com.example.codex.service.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun list(): Flux<User> = userService.allUsers()

    @GetMapping("/{id}")
    fun find(
        @PathVariable id: Long,
    ): Mono<User> = userService.find(id)

    @PostMapping("/register")
    fun register(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam externalId: String,
    ): Mono<Boolean> = userService.register(username, password, externalId)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): Mono<Int> = userService.delete(id)

    @GetMapping("/me")
    fun me(principal: java.security.Principal): Mono<User> = userService.findByExternalId(principal.name)

    @PostMapping("/{id}/privileges")
    fun updatePrivileges(
        @PathVariable id: Long,
        @RequestBody privilegeIds: List<Long>,
    ): Mono<Void> = userService.updatePrivileges(id, privilegeIds)
}
