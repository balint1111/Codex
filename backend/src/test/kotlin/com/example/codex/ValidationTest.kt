package com.example.codex

import com.example.codex.dto.UserRegistrationRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class ValidationTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    
    @Test
    fun `should validate user registration request correctly`() {
        // Valid request
        val validRequest = UserRegistrationRequest("testuser", "password123")
        val validViolations = validator.validate(validRequest)
        assertThat(validViolations).isEmpty()
        
        // Invalid username (too short)
        val invalidUsernameRequest = UserRegistrationRequest("ab", "password123")
        val usernameViolations = validator.validate(invalidUsernameRequest)
        assertThat(usernameViolations).isNotEmpty
        
        // Invalid password (too short)
        val invalidPasswordRequest = UserRegistrationRequest("testuser", "12345")
        val passwordViolations = validator.validate(invalidPasswordRequest)
        assertThat(passwordViolations).isNotEmpty
        
        // Empty username
        val emptyUsernameRequest = UserRegistrationRequest("", "password123")
        val emptyUsernameViolations = validator.validate(emptyUsernameRequest)
        assertThat(emptyUsernameViolations).isNotEmpty
    }
}