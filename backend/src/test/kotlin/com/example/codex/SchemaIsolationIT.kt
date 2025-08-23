package com.example.codex

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class SchemaIsolationIT : AbstractIntegrationTest() {
    @Test
    fun usesSchemaFromInitializer() {
        val expected = env.getRequiredProperty("test.schema")
        val current = jdbcTemplate.queryForObject("SELECT current_schema()", String::class.java)
        assertEquals(expected, current)
    }
}
