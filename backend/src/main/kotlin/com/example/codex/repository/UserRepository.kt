package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import com.example.codex.jooq.tables.Users as UsersTable
import com.example.codex.jooq.tables.references.PRIVILEGE
import com.example.codex.jooq.tables.references.USER_PRIVILEGE
import com.example.codex.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    override val dslContext: DSLContext
) : CrudRepository<UsersTable, User> {
    override val table = USERS
    override val type = User::class.java
    
    private val logger = LoggerFactory.getLogger(UserRepository::class.java)

    private fun baseUserQuery() = dslContext
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
        return try {
            baseUserQuery()
                .where(USERS.DELETED.eq(false))
                .fetchInto(User::class.java)
        } catch (ex: Exception) {
            logger.error("Error fetching all users", ex)
            throw ex
        }
    }

    fun save(username: String, password: String) {
        try {
            dslContext.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD, password)
                .execute()
            logger.debug("User saved successfully: {}", username)
        } catch (ex: Exception) {
            logger.error("Error saving user: {}", username, ex)
            if (ex.message?.contains("unique constraint") == true || 
                ex.message?.contains("duplicate key") == true) {
                throw DuplicateKeyException("Username already exists: $username")
            }
            throw ex
        }
    }

    fun softDelete(id: Long) {
        try {
            val updated = dslContext.update(USERS)
                .set(USERS.DELETED, true)
                .where(USERS.ID.eq(id))
                .execute()
            
            if (updated == 0) {
                logger.warn("No user found to delete with id: {}", id)
            } else {
                logger.debug("User soft deleted successfully: {}", id)
            }
        } catch (ex: Exception) {
            logger.error("Error soft deleting user with id: {}", id, ex)
            throw ex
        }
    }

    fun findByUsername(username: String): User? {
        return try {
            baseUserQuery()
                .where(USERS.USERNAME.eq(username).and(USERS.DELETED.eq(false)))
                .fetchOneInto(User::class.java)
        } catch (ex: Exception) {
            logger.error("Error finding user by username: {}", username, ex)
            throw ex
        }
    }

    fun addPrivilege(userId: Long, privilegeId: Long) {
        try {
            dslContext.insertInto(USER_PRIVILEGE)
                .set(USER_PRIVILEGE.USER_ID, userId)
                .set(USER_PRIVILEGE.PRIVILEGE_ID, privilegeId)
                .execute()
            logger.debug("Privilege added successfully: user {} privilege {}", userId, privilegeId)
        } catch (ex: Exception) {
            logger.error("Error adding privilege {} to user {}", privilegeId, userId, ex)
            throw ex
        }
    }

    fun updateUserPrivileges(userId: Long, privilegeIds: List<Long>) {
        try {
            dslContext.transaction { config ->
                val ctx = DSL.using(config)
                
                // Delete existing privileges
                val deleted = ctx.deleteFrom(USER_PRIVILEGE)
                    .where(USER_PRIVILEGE.USER_ID.eq(userId))
                    .execute()
                logger.debug("Deleted {} existing privileges for user {}", deleted, userId)
                
                // Insert new privileges
                privilegeIds.forEach { pid ->
                    ctx.insertInto(USER_PRIVILEGE)
                        .set(USER_PRIVILEGE.USER_ID, userId)
                        .set(USER_PRIVILEGE.PRIVILEGE_ID, pid)
                        .execute()
                }
                logger.debug("Added {} new privileges for user {}", privilegeIds.size, userId)
            }
        } catch (ex: Exception) {
            logger.error("Error updating privileges for user {}", userId, ex)
            throw ex
        }
    }

    fun findAllPrivileges(): List<Privilege> {
        return try {
            dslContext
                .select(*PRIVILEGE.fields())
                .from(PRIVILEGE)
                .fetchInto(Privilege::class.java)
        } catch (ex: Exception) {
            logger.error("Error fetching all privileges", ex)
            throw ex
        }
    }

    override fun findById(id: Long): User? {
        return try {
            baseUserQuery()
                .where(USERS.ID.eq(id).and(USERS.DELETED.eq(false)))
                .fetchOneInto(User::class.java)
        } catch (ex: Exception) {
            logger.error("Error finding user by id: {}", id, ex)
            throw ex
        }
    }
}
