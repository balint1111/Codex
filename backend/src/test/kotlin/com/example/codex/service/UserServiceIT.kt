package com.example.codex.service

import com.example.codex.AbstractIntegrationTest
import com.example.codex.repository.UserPrivilegeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier
import java.lang.Thread.sleep
import java.util.UUID

class UserServiceIT(
    @Autowired
    private val userService: UserService,
) : AbstractIntegrationTest() {
    @Test
    fun `registers user and appears in all users`() {
        val username = "sarah-${UUID.randomUUID()}"
        val externalId = "ext-$username"

        val testMono =
            userService
                .register(username, "secret", externalId)
                .then(userService.findByUsername(username))
                .flatMap { user ->
                    assertNotNull(user, "User should be found after registration")
                    userService
                        .allUsers()
                        .any { it.id == user.id }
                }

        StepVerifier
            .create(rollback(testMono))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `updates user privileges`() {
        val username = "sarah-${UUID.randomUUID()}"
        val externalId = "ext-$username"

        val testMono =
            userService
                .register(username, "secret", externalId)
                .then(userService.findByUsername(username))
                .flatMap { user ->
                    val id = requireNotNull(user.id)

                    userService
                        .allPrivileges()
                        .collectList()
                        .flatMap { privilegeList ->
                            val byName = privilegeList.associateBy { it.name }
                            val dashboard = requireNotNull(byName["dashboard"])
                            val users = requireNotNull(byName["users"])

                            userService
                                .updatePrivileges(id, listOf(dashboard.id, users.id))
                                .then(userService.find(id))
                                .doOnNext { updated ->
                                    val actual = updated.privileges.toSet()
                                    val expected = setOf(dashboard, users)
                                    assertEquals(expected, actual)
                                }
                        }
                }

        StepVerifier
            .create(rollback(testMono))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `deletes user`() {
        val username = "sarah-${UUID.randomUUID()}"
        val externalId = "ext-$username"

        val testMono =
            userService
                .register(username, "secret", externalId)
                .then(userService.findByUsername(username))
                .flatMap { user ->
                    val id = requireNotNull(user.id)

                    userService
                        .delete(id)
                        .then(userService.find(id)) // should be empty after delete
                }

        StepVerifier
            .create(rollback(testMono))
            .verifyComplete()
    }
}
