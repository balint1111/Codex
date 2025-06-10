package com.example.codex.repository

import com.example.codex.domain.UserPrivilege
import com.example.codex.jooq.tables.UserPrivilege as UserPrivilegeTable
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class UserPrivilegeRepository(
    override val dslContext: DSLContext
) : CrudRepository<UserPrivilegeTable, UserPrivilege> {
}
