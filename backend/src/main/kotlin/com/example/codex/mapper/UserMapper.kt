package com.example.codex.mapper

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import com.example.codex.dto.PrivilegeDto
import com.example.codex.dto.UserDto
import org.springframework.stereotype.Component

@Component
class UserMapper {
    
    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            privileges = user.privileges.map { toDto(it) },
            deleted = user.deleted
        )
    }
    
    fun toDto(privilege: Privilege): PrivilegeDto {
        return PrivilegeDto(
            id = privilege.id,
            name = privilege.name
        )
    }
    
    fun toDtoList(users: List<User>): List<UserDto> {
        return users.map { toDto(it) }
    }
    
    fun toPrivilegeDtoList(privileges: List<Privilege>): List<PrivilegeDto> {
        return privileges.map { toDto(it) }
    }
}