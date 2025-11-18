package com.example.codex.controller

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
@ExtendWith(value = [MockitoExtension::class])
class PrivilegeControllerIntegrationTest @Autowired constructor(
    private val webTestClient: WebTestClient,
) : AbstractIntegrationTest() {

    @MockBean
    private lateinit var jwtDecoder: ReactiveJwtDecoder

    @Test
    fun `should list privileges`() {
        val username = "user-" + UUID.randomUUID()
        val externalId = UUID.randomUUID().toString()

        // Register the user through the public API (whitelisted endpoint)
        webTestClient.post()
            .uri("/api/users/register")
            .body(
                BodyInserters.fromFormData("username", username)
                    .with("password", "password")
                    .with("externalId", externalId),
            )
            .exchange()
            .expectStatus().isOk

        val tokenValue = "dummy-token"
        val now = Instant.now()
        val jwt: Jwt =
            Jwt.withTokenValue(tokenValue)
                .header("alg", "none")
                .subject(externalId)
                .claim("preferred_username", username)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .build()

        given(jwtDecoder.decode(anyString())).willReturn(Mono.just(jwt))

        // Call the secured endpoint with a mocked JWT user
        webTestClient.get()
            .uri("/api/privileges")
            .headers { it.setBearerAuth(tokenValue) }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.name == 'dashboard')]").exists()
            .jsonPath("$[?(@.name == 'users')]").exists()
    }
}
