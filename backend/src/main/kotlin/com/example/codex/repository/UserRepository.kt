package com.example.codex.repository

import com.example.codex.domain.Privilege
import com.example.codex.domain.User
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val dSLContext: DSLContext
) {

    fun findAll(): List<User> {
        return dSLContext.fetch("SELECT * FROM users WHERE deleted = false").map { record ->
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
        print(dSLContext)
        dSLContext.execute("INSERT INTO users(username, password) VALUES (?, ?)", username, password)
    }

    fun softDelete(id: Long) {
        dSLContext.execute("UPDATE users SET deleted = true WHERE id = ?", id)
    }

    fun findByUsername(username: String): User? {
        val record = dSLContext.fetchOne("SELECT * FROM users WHERE username = ? AND deleted = false", username) ?: return null
        val userId = record.get("id", Long::class.java)!!
        return User(
            id = userId,
            username = record.get("username", String::class.java)!!,
            password = record.get("password", String::class.java)!!,
            privileges = findPrivilegesByUserId(userId)
        )
    }

    fun addPrivilege(userId: Long, privilegeId: Long) {
        dSLContext.execute("INSERT INTO user_privilege(user_id, privilege_id) VALUES (?, ?)", userId, privilegeId)
    }

    fun updateUserPrivileges(userId: Long, privilegeIds: List<Long>) {
        dSLContext.execute("DELETE FROM user_privilege WHERE user_id = ?", userId)
        privilegeIds.forEach { pid ->
            dSLContext.execute(
                "INSERT INTO user_privilege(user_id, privilege_id) VALUES (?, ?)",
                userId,
                pid
            )
        }
    }

    fun findAllPrivileges(): List<Privilege> {
        return dSLContext.fetch("SELECT id, name FROM privilege").map { r ->
            Privilege(
                r.get("id", Long::class.java)!!,
                r.get("name", String::class.java)!!
            )
        }
    }

    fun findById(id: Long): User? {
        val record = dSLContext.fetchOne("SELECT * FROM users WHERE id = ? AND deleted = false", id) ?: return null
        val userId = record.get("id", Long::class.java)!!
        return User(
            id = userId,
            username = record.get("username", String::class.java)!!,
            password = record.get("password", String::class.java)!!,
            privileges = findPrivilegesByUserId(userId)
        )
    }

    private fun findPrivilegesByUserId(userId: Long): List<Privilege> {
        return dSLContext.fetch(
            "SELECT p.id, p.name FROM privilege p JOIN user_privilege up ON p.id = up.privilege_id WHERE up.user_id = ?",
            userId
        )
            .map { r -> Privilege(r.get("id", Long::class.java)!!, r.get("name", String::class.java)!!) }
    }
}
