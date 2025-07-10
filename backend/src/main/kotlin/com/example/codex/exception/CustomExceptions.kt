package com.example.codex.exception

class UserNotFoundException(message: String) : RuntimeException(message)

class UserAlreadyExistsException(message: String) : RuntimeException(message)

class ValidationException(message: String) : RuntimeException(message)