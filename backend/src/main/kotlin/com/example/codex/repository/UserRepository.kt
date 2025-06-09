package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
open class UserRepository(private val ctx: DSLContext) {
    fun findAll(): List<User> {
        return ctx.fetch("SELECT * FROM users WHERE deleted = false").map { record ->
            val userId = record.get("id", Long::class.java)!!
            User(
                id = userId,
                username = record.get("username", String::class.java)!!,
                password = record.get("password", String::class.java)!!,
                privileges = findPrivilegesByUserId(userId)
            )
        }
    }

    fun save(username: String, password: String) {
        ctx.execute("INSERT INTO users(username, password) VALUES (?, ?)", username, password)
    }

    fun softDelete(id: Long) {
        ctx.execute("UPDATE users SET deleted = true WHERE id = ?", id)
    }

    fun findByUsername(username: String): User? {
        val record = ctx.fetchOne("SELECT * FROM users WHERE username = ? AND deleted = false", username) ?: return null
        val userId = record.get("id", Long::class.java)!!
        return User(
            id = userId,
            username = record.get("username", String::class.java)!!,
            password = record.get("password", String::class.java)!!,
            privileges = findPrivilegesByUserId(userId)
        )
    }

    fun addPrivilege(userId: Long, privilegeId: Long) {
        ctx.execute("INSERT INTO user_privilege(user_id, privilege_id) VALUES (?, ?)", userId, privilegeId)
    }

    fun updateUserPrivileges(userId: Long, privilegeIds: List<Long>) {
        ctx.execute("DELETE FROM user_privilege WHERE user_id = ?", userId)
        privilegeIds.forEach { pid ->
            ctx.execute(
                "INSERT INTO user_privilege(user_id, privilege_id) VALUES (?, ?)",
                userId,
                pid
            )
        }
    }

    fun findAllPrivileges(): List<Privilege> {
        return ctx.fetch("SELECT id, name FROM privilege").map { r ->
            Privilege(
                r.get("id", Long::class.java)!!,
                r.get("name", String::class.java)!!
            )
        }
    }

    fun findById(id: Long): User? {
        val record = ctx.fetchOne("SELECT * FROM users WHERE id = ? AND deleted = false", id) ?: return null
        val userId = record.get("id", Long::class.java)!!
        return User(
            id = userId,
            username = record.get("username", String::class.java)!!,
            password = record.get("password", String::class.java)!!,
            privileges = findPrivilegesByUserId(userId)
        )
    }

    private fun findPrivilegesByUserId(userId: Long): List<Privilege> {
        return ctx.fetch("SELECT p.id, p.name FROM privilege p JOIN user_privilege up ON p.id = up.privilege_id WHERE up.user_id = ?", userId)
            .map { r -> Privilege(r.get("id", Long::class.java)!!, r.get("name", String::class.java)!!) }
    }
}
