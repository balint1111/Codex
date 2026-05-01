package com.example.codex.repository

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier

class UserRepositoryIT(
    @Autowired
    private val userRepository: UserRepository,
) : AbstractIntegrationTest() {
    @Test
    fun `saves and retrieves user by username`() {
        val test =
            userRepository
                .save("jane", "secret", "ext-jane")
                .then(userRepository.findByUsername("jane"))

        StepVerifier
            .create(test)
            .assertNext { user ->
                assertNotNull(user)
                assertEquals("jane", user.username)
            }.verifyComplete()
    }

    @Test
    fun `soft deletes user`() {
        val test =
            userRepository
                .save("john", "secret", "ext-john")
                .flatMap { userRepository.findByUsername("john") }
                .flatMap { user ->
                    println("soft delete user $user")
                    userRepository
                        .softDelete(user.id)
                        .then(userRepository.findById(user.id).hasElement())
                }

        StepVerifier
            .create(test)
            .assertNext { assertEquals(false, it) }
            .verifyComplete()
    }

    @Test
    fun `manages user privileges`() {
        val test =
            userRepository
                .save("seed", "pw", "ext-seed")
                .then(userRepository.save("mike", "pw", "ext-mike"))
                .then(userRepository.findByUsername("mike"))
                .flatMap { mike ->
                    val mikeId = mike.id
                    userRepository
                        .findAllPrivileges()
                        .collectList()
                        .flatMap { privileges ->
                            val privilegeMap = privileges.associateBy { it.name }
                            val dashboard = privilegeMap["dashboard"]!!
                            val users = privilegeMap["users"]!!

                            userRepository
                                .addPrivilege(mikeId, dashboard.id)
                                .then(userRepository.findById(mikeId))
                                .doOnNext { user ->
                                    assertEquals(listOf(dashboard), user.privileges)
                                }.then(userRepository.updateUserPrivileges(mikeId, listOf(users.id)))
                                .then(userRepository.findById(mikeId))
                                .doOnNext { user ->
                                    assertEquals(listOf(users), user.privileges)
                                }
                        }
                }

        StepVerifier
            .create(test)
            .expectNextCount(1)
            .verifyComplete()
    }
}
