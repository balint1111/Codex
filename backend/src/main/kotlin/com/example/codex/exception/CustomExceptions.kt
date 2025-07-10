package com.example.codex.exception

class UserNotFoundException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class DuplicateUsernameException(message: String) : RuntimeException(message)