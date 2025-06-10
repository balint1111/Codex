package com.example.codex.repository

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Table

/**
 * Generic CRUD repository using jOOQ.
 */
interface CrudRepository<TABLE : Table<out Record>, POJO> {
    val dslContext: DSLContext
    val table: TABLE
    val type: Class<POJO>

    fun findAll(): List<POJO> =
        dslContext.select(*table.fields())
            .from(table)
            .fetchInto(type)

    fun findById(id: Long): POJO? =
        dslContext.select(*table.fields())
            .from(table)
            .where(table.field("id", Long::class.java)!!.eq(id))
            .fetchOneInto(type)

    fun insert(pojo: POJO) {
        val record = dslContext.newRecord(table)
        record.from(pojo)
        (record as org.jooq.UpdatableRecord<*>).insert()
    }

    fun update(pojo: POJO) {
        val record = dslContext.newRecord(table)
        record.from(pojo)
        (record as org.jooq.UpdatableRecord<*>).update()
    }

    fun delete(id: Long) {
        dslContext.deleteFrom(table)
            .where(table.field("id", Long::class.java)!!.eq(id))
            .execute()
    }
}
