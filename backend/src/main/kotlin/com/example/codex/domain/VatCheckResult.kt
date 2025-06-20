package com.example.codex.domain

data class VatCheckResult(
    val valid: Boolean,
    val name: String?,
    val address: String?
)
