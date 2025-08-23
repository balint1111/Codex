package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import com.example.codex.jooq.tables.references.PRIVILEGE
import com.example.codex.jooq.tables.references.USERS
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import com.example.codex.jooq.tables.Users as UsersTable

@Repository
class UserRepository(
    override val dslContext: DSLContext,
) : CrudRepository<UsersTable, User> {
    override val table = USERS
    override val type = User::class.java

    private fun baseUserQuery() =
        dslContext
            .select(
                *USERS.fields(),
                DSL
                    .multiset(
                        DSL
                            .select(PRIVILEGE.ID, PRIVILEGE.NAME)
                            .from(PRIVILEGE)
                            .join(USER_PRIVILEGE)
                            .on(PRIVILEGE.ID.eq(USER_PRIVILEGE.PRIVILEGE_ID))
                            .where(USER_PRIVILEGE.USER_ID.eq(USERS.ID)),
                    ).convertFrom { it.into(Privilege::class.java) }
                    .`as`("privileges"),
            ).from(USERS)

    override fun findAll(): List<User> =
        baseUserQuery()
            .where(USERS.DELETED.eq(false))
            .fetchInto(User::class.java)

    fun save(
        username: String,
        password: String,
    ) {
        dslContext
            .insertInto(USERS)
            .set(USERS.USERNAME, username)
            .set(USERS.PASSWORD, password)
            .execute()
    }

    fun softDelete(id: Long) {
        dslContext
            .update(USERS)
            .set(USERS.DELETED, true)
            .where(USERS.ID.eq(id))
            .execute()
    }

    fun findByUsername(username: String): User? =
        baseUserQuery()
            .where(USERS.USERNAME.eq(username).and(USERS.DELETED.eq(false)))
            .fetchOneInto(User::class.java)

    fun addPrivilege(
        userId: Long,
        privilegeId: Long,
    ) {
        dslContext
            .insertInto(USER_PRIVILEGE)
            .set(USER_PRIVILEGE.USER_ID, userId)
            .set(USER_PRIVILEGE.PRIVILEGE_ID, privilegeId)
            .execute()
    }

    fun updateUserPrivileges(
        userId: Long,
        privilegeIds: List<Long>,
    ) {
        dslContext.transaction { config ->
            val ctx = DSL.using(config)
            ctx
                .deleteFrom(USER_PRIVILEGE)
                .where(USER_PRIVILEGE.USER_ID.eq(userId))
                .execute()
            privilegeIds.forEach { pid ->
                ctx
                    .insertInto(USER_PRIVILEGE)
                    .set(USER_PRIVILEGE.USER_ID, userId)
                    .set(USER_PRIVILEGE.PRIVILEGE_ID, pid)
                    .execute()
            }
        }
    }

    fun findAllPrivileges(): List<Privilege> =
        dslContext
            .select(*PRIVILEGE.fields())
            .from(PRIVILEGE)
            .fetchInto(Privilege::class.java)

    override fun findById(id: Long): User? =
        baseUserQuery()
            .where(USERS.ID.eq(id).and(USERS.DELETED.eq(false)))
            .fetchOneInto(User::class.java)
}
