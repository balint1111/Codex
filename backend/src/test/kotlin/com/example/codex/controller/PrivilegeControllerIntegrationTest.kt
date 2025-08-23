package com.example.codex.controller

import com.example.codex.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic

@AutoConfigureMockMvc
class PrivilegeControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should list privileges`() {
        mockMvc
            .perform(
                post("/api/users/register")
                    .param("username", "testuser")
                    .param("password", "password"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(get("/api/privileges").with(httpBasic("testuser", "password")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.name == 'dashboard')]").exists())
            .andExpect(jsonPath("$[?(@.name == 'users')]").exists())
    }
}

