package com.example.codex.controller

import com.example.codex.AbstractControllerITTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserControllerIT(
    @Autowired
    private val webTestClient: WebTestClient,
) : AbstractControllerITTest() {
    @Test
    fun `registers user and returns it from me`() {
        webTestClient
            .post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/users/register")
                    .queryParam("username", USERNAME)
                    .queryParam("password", "secret")
                    .queryParam("externalId", EXTERNAL_ID)
                    .build()
            }.exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/api/users/me")
            .headers { it.setBearerAuth("dummy-token") }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.username")
            .isEqualTo(USERNAME)
    }
}
