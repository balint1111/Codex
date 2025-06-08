package com.example.codex.service

import com.example.codex.domain.User
import com.example.codex.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun allUsers(): List<User> = userRepository.findAll()

    fun register(username: String, password: String) {
        userRepository.save(username, passwordEncoder.encode(password))
    }

    fun delete(id: Long) {
        userRepository.softDelete(id)
    }
}
