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
            userRepository.save("jane", "secret", "ext-jane").block()
            val user = userRepository.findByUsername("jane").block()
            assertNotNull(user)
            assertEquals("jane", user?.username)
        }

        @Test
        fun `soft deletes user`() {
            userRepository.save("john", "secret", "ext-john").block()
            val id = userRepository.findByUsername("john").block()?.id!!
            userRepository.softDelete(id).block()
            assertNull(userRepository.findById(id).block())
            assertNull(userRepository.findByUsername("john").block())
            assertTrue(userRepository.findAll().collectList().block()!!.none { it.id == id })
        }

        @Test
        fun `manages user privileges`() {
            // Ensure first user takes pre-seeded privileges
            userRepository.save("seed", "pw", "ext-seed").block()
            userRepository.save("mike", "pw", "ext-mike").block()
            val mikeId = userRepository.findByUsername("mike").block()!!.id!!

            val privilegeMap = userRepository.findAllPrivileges().collectList().block()!!.associateBy { it.name }
            val dashboard = privilegeMap["dashboard"]!!
            val users = privilegeMap["users"]!!

            userRepository.addPrivilege(mikeId, dashboard.id).block()
            var user = userRepository.findById(mikeId).block()
            assertEquals(listOf(dashboard), user?.privileges)

            userRepository.updateUserPrivileges(mikeId, listOf(users.id)).block()
            user = userRepository.findById(mikeId).block()
            assertEquals(listOf(users), user?.privileges)
        }
    }
