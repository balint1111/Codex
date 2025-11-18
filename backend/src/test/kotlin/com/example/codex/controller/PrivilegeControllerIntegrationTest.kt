package com.example.codex.controller

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import java.util.UUID

@SpringBootTest(
    properties = [
        // Needed so SecurityConfig.jwtDecoder() can be created in tests
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/dummy",
        // If not already provided by your test config:
        "frontendUrl=http://localhost:3000"
    ]
)
@AutoConfigureMockMvc
class PrivilegeControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) : AbstractIntegrationTest() {

    @Test
    fun `should list privileges`() {
        val username = "user-" + UUID.randomUUID()
        val externalId = UUID.randomUUID().toString()

        // Register the user through the public API (whitelisted endpoint)
        mockMvc
            .perform(
                post("/api/users/register")
                    .param("username", username)
                    .param("password", "password")
                    .param("externalId", externalId),
            )
            .andExpect(status().isOk)

        // Call the secured endpoint with a mocked JWT user
        val mvcResult =
            mockMvc
                .perform(
                    get("/api/privileges").with(
                        jwt().jwt {
                            it.subject(externalId)                   // -> jwt.subject
                            it.claim("preferred_username", username) // extra claim if you need it
                        }
                    )
                )
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc
            .perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andDo(print())
            // if /api/privileges returns a list of objects like [{ "name": "dashboard" }, ...]
            .andExpect(jsonPath("$[?(@.name == 'dashboard')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'users')]").exists())
    }
}
