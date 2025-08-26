package com.example.codex.repository

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserRepositoryIT
    @Autowired
    constructor(
        private val userRepository: UserRepository,
    ) : AbstractIntegrationTest() {
        @Test
        fun `saves and retrieves user by username`() {
            userRepository.save("jane", "secret", "ext-jane")
            val user = userRepository.findByUsername("jane")
            assertNotNull(user)
            assertEquals("jane", user?.username)
        }

        @Test
        fun `soft deletes user`() {
            userRepository.save("john", "secret", "ext-john")
            val id = userRepository.findByUsername("john")!!.id
            userRepository.softDelete(id)
            assertNull(userRepository.findById(id))
            assertNull(userRepository.findByUsername("john"))
            assertTrue(userRepository.findAll().none { it.id == id })
        }

        @Test
        fun `manages user privileges`() {
            // Ensure first user takes pre-seeded privileges
            userRepository.save("seed", "pw", "ext-seed")
            userRepository.save("mike", "pw", "ext-mike")
            val mikeId = userRepository.findByUsername("mike")!!.id

            val privilegeMap = userRepository.findAllPrivileges().associateBy { it.name }
            val dashboard = privilegeMap["dashboard"]!!
            val users = privilegeMap["users"]!!

            userRepository.addPrivilege(mikeId, dashboard.id)
            var user = userRepository.findById(mikeId)
            assertEquals(listOf(dashboard), user?.privileges)

            userRepository.updateUserPrivileges(mikeId, listOf(users.id))
            user = userRepository.findById(mikeId)
            assertEquals(listOf(users), user?.privileges)
        }
    }
