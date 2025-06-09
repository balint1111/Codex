package com.example.codex

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class CodexApplication

fun main(args: Array<String>) {
    runApplication<CodexApplication>(*args)
}
