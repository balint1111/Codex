package com.example.codex.repository

import com.example.codex.jooq.tables.pojos.UserPrivilege
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL.field
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import com.example.codex.jooq.tables.UserPrivilege as UserPrivilegeTable

@Repository
class UserPrivilegeRepository(
    override val dslContext: DSLContext,
) : CrudRepository<UserPrivilegeTable, UserPrivilege, Long> {
    override val table = USER_PRIVILEGE
    override val type = UserPrivilege::class.java
    override val keyType = Long::class.java
    override val keyField: Field<Long> = field(USER_PRIVILEGE.ID.name, Long::class.java)

    fun saveAll(
        pojos: Flux<UserPrivilege>,
        batchSize: Int = 500,
    ): Flux<UserPrivilege> = super.saveAll(pojos, batchSize, listOf(USER_PRIVILEGE.USER_ID, USER_PRIVILEGE.PRIVILEGE_ID))
}
