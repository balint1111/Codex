package com.example.codex.service

import com.example.codex.domain.User
import com.example.codex.exception.UserNotFoundException
import com.example.codex.exception.DuplicateUsernameException
import com.example.codex.repository.UserRepository
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.dao.DataIntegrityViolationException
import org.slf4j.LoggerFactory

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    fun allUsers(): List<User> = userRepository.findAll()

    fun register(username: String, rawPassword: String) {
        try {
            logger.info("Attempting to register user: $username")
            userRepository.save(username, passwordEncoder.encode(rawPassword))
            logger.info("Successfully registered user: $username")
        } catch (e: DataIntegrityViolationException) {
            logger.error("Failed to register user due to duplicate username: $username", e)
            throw DuplicateUsernameException("Username '$username' is already taken")
        } catch (e: Exception) {
            logger.error("Unexpected error during user registration for: $username", e)
            throw e
        }
    }

    fun delete(id: Long) {
        logger.info("Attempting to delete user with id: $id")
        val user = userRepository.findById(id)
        if (user == null) {
            logger.warn("Attempted to delete non-existent user with id: $id")
            throw UserNotFoundException("User with id $id not found")
        }
        userRepository.softDelete(id)
        logger.info("Successfully deleted user with id: $id")
    }

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun updatePrivileges(userId: Long, privilegeIds: List<Long>) {
        logger.info("Updating privileges for user id: $userId")
        val user = userRepository.findById(userId)
        if (user == null) {
            logger.warn("Attempted to update privileges for non-existent user with id: $userId")
            throw UserNotFoundException("User with id $userId not found")
        }
        userRepository.updateUserPrivileges(userId, privilegeIds)
        logger.info("Successfully updated privileges for user id: $userId")
    }

    fun allPrivileges() = userRepository.findAllPrivileges()

    fun find(id: Long): User? = userRepository.findById(id)
}
