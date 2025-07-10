package com.example.codex.controller

import com.example.codex.dto.PrivilegeDto
import com.example.codex.mapper.UserMapper
import com.example.codex.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/privileges")
class PrivilegeController(
    private val userService: UserService,
    private val userMapper: UserMapper
) {
    
    private val logger = LoggerFactory.getLogger(PrivilegeController::class.java)
    
    @GetMapping
    fun list(): ResponseEntity<List<PrivilegeDto>> {
        logger.info("Fetching all privileges")
        val privileges = userService.allPrivileges()
        return ResponseEntity.ok(userMapper.toPrivilegeDtoList(privileges))
    }
}
