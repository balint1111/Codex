package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import com.example.codex.jooq.tables.pojos.Users
import com.example.codex.jooq.tables.references.PRIVILEGE
import com.example.codex.jooq.tables.references.USERS
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.example.codex.jooq.tables.Users as UsersTable

@Repository
class UserRepository(
    override val dslContext: DSLContext,
) : CrudRepository<UsersTable, User, Long> {
    override val table = USERS
    override val type = User::class.java
    override val keyType = Long::class.java
    override val keyField: Field<Long> = table.field("id", keyType)!!

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

    override fun findAll(): Flux<User> =
        baseUserQuery()
            .where(USERS.DELETED.eq(false))
            .let { Flux.from(it) }
            .map { it.into(User::class.java)!! }

    fun save(
        username: String,
        password: String,
        externalId: String,
    ) = dslContext
        .also { println("userName :$username password:$password externalId:$externalId") }
            .insertInto(USERS)
            .set(USERS.USERNAME, username)
            .set(USERS.PASSWORD, password)
            .set(USERS.EXTERNAL_ID, externalId)
            .returning(*USERS.fields())
            .let { Mono.from(it) }
            .map {
                println("fetched: $it")
                it.into(Users::class.java) }


    fun softDelete(id: Long) =
        dslContext
            .update(USERS)
            .set(USERS.DELETED, true)
            .where(USERS.ID.eq(id))
            .let { Mono.from(it) }

    fun findByUsername(username: String) =
        Mono.from(
            baseUserQuery()
                .where(USERS.USERNAME.eq(username).and(USERS.DELETED.eq(false)))
        ).map { it.into(User::class.java) }


    fun findByExternalId(externalId: String): Mono<User> =
        baseUserQuery()
            .where(USERS.EXTERNAL_ID.eq(externalId).and(USERS.DELETED.eq(false)))
            .let { Mono.from(it) }
            .map { it.into(User::class.java) }

    fun addPrivilege(
        userId: Long,
        privilegeId: Long,
    ) = dslContext
            .insertInto(USER_PRIVILEGE)
            .set(USER_PRIVILEGE.USER_ID, userId)
            .set(USER_PRIVILEGE.PRIVILEGE_ID, privilegeId)
            .let { Mono.from(it) }

    fun updateUserPrivileges(
        userId: Long,
        privilegeIds: List<Long>,
    ): Mono<Void> =
        Mono.from(
            dslContext.deleteFrom(USER_PRIVILEGE)
                .where(USER_PRIVILEGE.USER_ID.eq(userId))
        )
            .thenMany(
                Flux.fromIterable(privilegeIds)
                    .concatMap { pid ->
                        Mono.from(
                            dslContext.insertInto(USER_PRIVILEGE)
                                .set(USER_PRIVILEGE.USER_ID, userId)
                                .set(USER_PRIVILEGE.PRIVILEGE_ID, pid)
                        )
                    }
            ).then()



    fun findAllPrivileges(): Flux<Privilege> =
        dslContext
            .select(*PRIVILEGE.fields())
            .from(PRIVILEGE)
            .let { Flux.from(it) }
            .map { it.into(Privilege::class.java) }

    override fun findById(id: Long): Mono<User> =
        baseUserQuery()
            .where(USERS.ID.eq(id).and(USERS.DELETED.eq(false)))
            .let { Mono.from(it) }
            .map { it.into(User::class.java) }
}
