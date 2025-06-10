package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import com.example.codex.jooq.tables.Users as UsersTable
import com.example.codex.jooq.tables.references.PRIVILEGE
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import com.example.codex.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    override val dsl: DSLContext
) : CrudRepository<UsersTable, User> {
    override val table = USERS
    override val type = User::class.java

    private fun baseUserQuery() = dsl
        .select(
            *USERS.fields(),
            DSL.multiset(
                DSL.select(PRIVILEGE.ID, PRIVILEGE.NAME)
                    .from(PRIVILEGE)
                    .join(USER_PRIVILEGE)
                    .on(PRIVILEGE.ID.eq(USER_PRIVILEGE.PRIVILEGE_ID))
                    .where(USER_PRIVILEGE.USER_ID.eq(USERS.ID))
            ).convertFrom { it.into(Privilege::class.java) }.`as`("privileges")
        )
        .from(USERS)

    override fun findAll(): List<User> {
        return baseUserQuery()
            .where(USERS.DELETED.eq(false))
            .fetchInto(User::class.java)
    }

    fun save(username: String, password: String) {
        dsl.insertInto(USERS)
            .set(USERS.USERNAME, username)
            .set(USERS.PASSWORD, password)
            .execute()
    }

    fun softDelete(id: Long) {
        dsl.update(USERS)
            .set(USERS.DELETED, true)
            .where(USERS.ID.eq(id))
            .execute()
    }

    fun findByUsername(username: String): User? {
        return baseUserQuery()
            .where(USERS.USERNAME.eq(username).and(USERS.DELETED.eq(false)))
            .fetchOneInto(User::class.java)
    }

    fun addPrivilege(userId: Long, privilegeId: Long) {
        dsl.insertInto(USER_PRIVILEGE)
            .set(USER_PRIVILEGE.USER_ID, userId)
            .set(USER_PRIVILEGE.PRIVILEGE_ID, privilegeId)
            .execute()
    }

    fun updateUserPrivileges(userId: Long, privilegeIds: List<Long>) {
        dsl.transaction { config ->
            val ctx = DSL.using(config)
            ctx.deleteFrom(USER_PRIVILEGE)
                .where(USER_PRIVILEGE.USER_ID.eq(userId))
                .execute()
            privilegeIds.forEach { pid ->
                ctx.insertInto(USER_PRIVILEGE)
                    .set(USER_PRIVILEGE.USER_ID, userId)
                    .set(USER_PRIVILEGE.PRIVILEGE_ID, pid)
                    .execute()
            }
        }
    }

    fun findAllPrivileges(): List<Privilege> {
        return dsl
            .select(*PRIVILEGE.fields()).from(PRIVILEGE)
            .fetchInto(Privilege::class.java)
    }

    override fun findById(id: Long): User? {
        return baseUserQuery()
            .where(USERS.ID.eq(id).and(USERS.DELETED.eq(false)))
            .fetchOneInto(User::class.java)
    }

}
