package com.example.codex.service

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserServiceIT
    @Autowired
    constructor(
        private val userService: UserService,
    ) : AbstractIntegrationTest() {
        @Test
        fun `registers, updates privileges and deletes user`() {
            userService.register("sarah", "secret")
            val user = userService.findByUsername("sarah")
            assertNotNull(user)
            val id = user!!.id
            assertTrue(userService.allUsers().any { it.id == id })

            val privileges = userService.allPrivileges().associateBy { it.name }
            val dashboard = privileges["dashboard"]!!
            val users = privileges["users"]!!

            userService.updatePrivileges(id, listOf(dashboard.id, users.id))
            val updated = userService.find(id)
            assertEquals(setOf(dashboard, users), updated?.privileges?.toSet())

            userService.delete(id)
            assertNull(userService.find(id))
        }
    }
