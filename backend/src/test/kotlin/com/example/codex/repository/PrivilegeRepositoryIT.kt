package com.example.codex.repository

import com.example.codex.AbstractIntegrationTest
import com.example.codex.domain.Privilege
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PrivilegeRepositoryIT
    @Autowired
    constructor(
        private val privilegeRepository: PrivilegeRepository,
    ) : AbstractIntegrationTest() {
        @Test
        fun `performs CRUD operations`() {
            // insert
            val privilege = Privilege(999L, "reports")
            privilegeRepository.insert(privilege)

            // find all
            val all = privilegeRepository.findAll()
            assertTrue(all.any { it.id == 999L && it.name == "reports" })

            // find by id
            val found = privilegeRepository.findById(999L)
            assertNotNull(found)
            assertEquals("reports", found?.name)

            // update
            val updatedPrivilege = Privilege(999L, "reports-updated")
            privilegeRepository.update(updatedPrivilege)
            val updated = privilegeRepository.findById(999L)
            assertEquals("reports-updated", updated?.name)

            // delete
            privilegeRepository.delete(999L)
            val deleted = privilegeRepository.findById(999L)
            assertNull(deleted)
        }
    }
