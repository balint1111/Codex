package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.jooq.tables.Privilege as PrivilegeTable
import com.example.codex.jooq.tables.references.PRIVILEGE
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class PrivilegeRepository(
    override val dslContext: DSLContext
) : CrudRepository<PrivilegeTable, Privilege> {
    override val table = PRIVILEGE
    override val type = Privilege::class.java
}
