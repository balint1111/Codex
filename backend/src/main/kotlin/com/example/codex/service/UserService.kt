package com.example.codex.service

import com.example.codex.domain.User
import com.example.codex.jooq.tables.pojos.UserPrivilege
import com.example.codex.repository.PrivilegeRepository
import com.example.codex.repository.UserPrivilegeRepository
import com.example.codex.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userPrivilegeRepository: UserPrivilegeRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun allUsers() = userRepository.findAll()

    fun register(
        username: String,
        password: String,
        externalId: String,
    ): Mono<Boolean> = userRepository.save(username, passwordEncoder.encode(password), externalId).log()
        .doOnNext { user ->
            println("register user: $user")
            userPrivilegeRepository.saveAll(
                Flux.fromIterable(listOf(1L, 2L))
                    .map { UserPrivilege(userId = user.id!!, privilegeId = it) },
            ).doOnNext { value -> System.out.println("Saw: " + value) }
        }.map { it != null }.log()

    fun delete(id: Long): Mono<Int> {
        return userRepository.softDelete(id)
    }

    fun realDelete(id: Long) = userRepository.delete(id)


    fun findByUsername(username: String) = userRepository.findByUsername(username)

    fun findByExternalId(externalId: String): Mono<User> = userRepository.findByExternalId(externalId).doOnNext { println("found user: $it") }

    @Transactional
    fun updatePrivileges(
        userId: Long,
        privilegeIds: List<Long>,
    ): Mono<Void> {
        return userRepository.updateUserPrivileges(userId, privilegeIds)
    }

    fun allPrivileges() = userRepository.findAllPrivileges()

    fun find(id: Long) = userRepository.findById(id)
}
