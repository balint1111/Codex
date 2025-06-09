package com.example.codex.service

import com.example.codex.domain.User
import com.example.codex.repository.UserRepository
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun allUsers(): List<User> = userRepository.findAll()

    fun register(username: String, encodedPassword: String) {
        userRepository.save(username, passwordEncoder.encode(encodedPassword))
    }

    fun delete(id: Long) {
        userRepository.softDelete(id)
    }

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun updatePrivileges(userId: Long, privilegeIds: List<Long>) {
        userRepository.updateUserPrivileges(userId, privilegeIds)
    }

    fun allPrivileges() = userRepository.findAllPrivileges()

    fun find(id: Long): User? = userRepository.findById(id)
}
