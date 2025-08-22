package com.example.codex.repository

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserRepositoryIT @Autowired constructor(
    private val userRepository: UserRepository,

) : AbstractIntegrationTest() {

    @Test
    fun `saves and retrieves user by username`() {
        userRepository.save("jane", "secret")
        val user = userRepository.findByUsername("jane")
        assertNotNull(user)
        assertEquals("jane", user?.username)
    }
}
