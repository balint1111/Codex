package com.example.codex.service

import com.example.codex.domain.User
import com.example.codex.exception.UserAlreadyExistsException
import com.example.codex.exception.UserNotFoundException
import com.example.codex.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    fun allUsers(): List<User> {
        logger.debug("Fetching all users")
        return userRepository.findAll()
    }

    fun register(username: String, password: String) {
        logger.info("Registering user: {}", username)
        
        // Check if user already exists
        if (userRepository.findByUsername(username) != null) {
            throw UserAlreadyExistsException("User with username '$username' already exists")
        }
        
        try {
            userRepository.save(username, passwordEncoder.encode(password))
            logger.info("User registered successfully: {}", username)
        } catch (ex: DuplicateKeyException) {
            throw UserAlreadyExistsException("User with username '$username' already exists")
        }
    }

    fun delete(id: Long) {
        logger.info("Deleting user with id: {}", id)
        
        // Check if user exists
        if (userRepository.findById(id) == null) {
            throw UserNotFoundException("User with id $id not found")
        }
        
        userRepository.softDelete(id)
        logger.info("User deleted successfully: {}", id)
    }

    fun findByUsername(username: String): User? {
        logger.debug("Finding user by username: {}", username)
        return userRepository.findByUsername(username)
    }

    fun updatePrivileges(userId: Long, privilegeIds: List<Long>) {
        logger.info("Updating privileges for user id: {} with privileges: {}", userId, privilegeIds)
        
        // Check if user exists
        if (userRepository.findById(userId) == null) {
            throw UserNotFoundException("User with id $userId not found")
        }
        
        userRepository.updateUserPrivileges(userId, privilegeIds)
        logger.info("Privileges updated successfully for user: {}", userId)
    }

    fun allPrivileges() = userRepository.findAllPrivileges()

    fun find(id: Long): User? {
        logger.debug("Finding user by id: {}", id)
        return userRepository.findById(id)
    }
}
