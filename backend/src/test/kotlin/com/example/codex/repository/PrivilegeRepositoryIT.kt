package com.example.codex.repository

import com.example.codex.AbstractIntegrationTest
import com.example.codex.domain.Privilege
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

class PrivilegeRepositoryIT(
    @Autowired
    private val privilegeRepository: PrivilegeRepository,
) : AbstractIntegrationTest() {
    @Test
    fun `performs CRUD operations`() {
        val privilege = Privilege(999L, "reports")
        val updatedPrivilege = Privilege(999L, "reports-updated")

        val test =
            privilegeRepository
                .insert(privilege)
                .then(privilegeRepository.findAll().collectList())
                .doOnNext { all ->
                    assertTrue(all.any { it.id == 999L && it.name == "reports" })
                }.then(privilegeRepository.findById(999L))
                .doOnNext { found ->
                    assertNotNull(found)
                    assertEquals("reports", found.name)
                }.then(privilegeRepository.update(updatedPrivilege))
                .then(privilegeRepository.findById(999L))
                .doOnNext { updated ->
                    assertEquals("reports-updated", updated.name)
                }.then(privilegeRepository.delete(999L))
                .then(privilegeRepository.findById(999L))

        StepVerifier
            .create(test)
            .expectNextCount(0)
            .verifyComplete()
    }
}
