package com.example.codex.controller

import com.example.codex.AbstractControllerITTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient(timeout = "PT20S")
class UserControllerIT
    @Autowired
    constructor(
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
