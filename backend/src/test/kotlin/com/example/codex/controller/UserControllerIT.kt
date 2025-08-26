package com.example.codex.controller

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@AutoConfigureMockMvc
class UserControllerIT @Autowired constructor(
    private val mockMvc: MockMvc,
) : AbstractIntegrationTest() {
    @Test
    fun `registers user and returns it from me`() {
        val username = "user-" + UUID.randomUUID()
        val externalId = UUID.randomUUID().toString()

        mockMvc
            .perform(
                post("/api/users/register")
                    .param("username", username)
                    .param("password", "secret")
                    .param("externalId", externalId),
            )
            .andExpect(status().isOk)

        mockMvc
            .perform(get("/api/users/me").with(jwt().jwt { it.subject(externalId) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(username))
    }
}
