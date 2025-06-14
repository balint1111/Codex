/*
 * This file is generated by jOOQ.
 */
package com.example.codex.jooq.tables.records


import com.example.codex.jooq.tables.Users

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record4
import org.jooq.Row4
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("warnings")
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "CONSTRAINT_4D", columnNames = [ "USERNAME" ])
    ]
)
open class UsersRecord private constructor() : UpdatableRecordImpl<UsersRecord>(Users.USERS), Record4<Long?, String?, String?, Boolean?> {

    @get:Id
    @get:GeneratedValue(strategy = GenerationType.IDENTITY)
    @get:Column(name = "id", nullable = false)
    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    @get:Column(name = "username", nullable = false, length = 50)
    open var username: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    @get:Column(name = "password", length = 100)
    open var password: String?
        set(value): Unit = set(2, value)
        get(): String? = get(2) as String?

    @get:Column(name = "deleted")
    open var deleted: Boolean?
        set(value): Unit = set(3, value)
        get(): Boolean? = get(3) as Boolean?

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row4<Long?, String?, String?, Boolean?> = super.fieldsRow() as Row4<Long?, String?, String?, Boolean?>
    override fun valuesRow(): Row4<Long?, String?, String?, Boolean?> = super.valuesRow() as Row4<Long?, String?, String?, Boolean?>
    override fun field1(): Field<Long?> = Users.USERS.ID
    override fun field2(): Field<String?> = Users.USERS.USERNAME
    override fun field3(): Field<String?> = Users.USERS.PASSWORD
    override fun field4(): Field<Boolean?> = Users.USERS.DELETED
    override fun component1(): Long? = id
    override fun component2(): String = username
    override fun component3(): String? = password
    override fun component4(): Boolean? = deleted
    override fun value1(): Long? = id
    override fun value2(): String = username
    override fun value3(): String? = password
    override fun value4(): Boolean? = deleted

    override fun value1(value: Long?): UsersRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): UsersRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): UsersRecord {
        set(2, value)
        return this
    }

    override fun value4(value: Boolean?): UsersRecord {
        set(3, value)
        return this
    }

    override fun values(value1: Long?, value2: String?, value3: String?, value4: Boolean?): UsersRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        return this
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    constructor(id: Long? = null, username: String, password: String? = null, deleted: Boolean? = null): this() {
        this.id = id
        this.username = username
        this.password = password
        this.deleted = deleted
        resetTouchedOnNotNull()
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    constructor(value: com.example.codex.jooq.tables.pojos.Users?): this() {
        if (value != null) {
            this.id = value.id
            this.username = value.username
            this.password = value.password
            this.deleted = value.deleted
            resetTouchedOnNotNull()
        }
    }
}
