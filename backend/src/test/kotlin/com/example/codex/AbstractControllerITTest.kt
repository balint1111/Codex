package com.example.codex

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest
abstract class AbstractControllerITTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var myJwtDecoder: ReactiveJwtDecoder

    companion object {
        val USERNAME = "user-" + UUID.randomUUID()
        val EXTERNAL_ID = UUID.randomUUID().toString()
    }

    @BeforeEach
    fun setup() {
        val now = Instant.now()
        val jwt: Jwt =
            Jwt
                .withTokenValue("dummy-token")
                .header("alg", "none")
                .subject(EXTERNAL_ID) // jwt.subject
                .claim("preferred_username", USERNAME) // extra claim used by your app
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .build()
        every { myJwtDecoder.decode(any()) } returns Mono.just(jwt)
    }
}
