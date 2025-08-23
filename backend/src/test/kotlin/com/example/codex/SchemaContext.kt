package com.example.codex

object SchemaContext {
    private val current = ThreadLocal<String?>()

    fun set(schema: String) = current.set(schema)
    fun get(): String = current.get() ?: "public"
}
