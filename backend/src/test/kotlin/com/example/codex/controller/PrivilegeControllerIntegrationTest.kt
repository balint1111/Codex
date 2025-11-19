package com.example.codex.controller

import com.example.codex.AbstractControllerITTest
import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PrivilegeControllerIntegrationTest(
    @Autowired
    private val webTestClient: WebTestClient,
) : AbstractControllerITTest() {
    @Test
    fun `should list privileges`() {
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
            .uri("/api/privileges")
            .headers { it.setBearerAuth("dummy-token") }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$[?(@.name == 'dashboard')]")
            .exists()
            .jsonPath("$[?(@.name == 'users')]")
            .exists()
    }
}
