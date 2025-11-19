package com.example.codex.repository

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.conf.ParamType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Generic CRUD repository using jOOQ.
 */
interface CrudRepository<TABLE : Table<out Record>, POJO : Any, KEY> {
    val dslContext: DSLContext
    val table: TABLE
    val type: Class<POJO>
    val keyType: Class<KEY>
    val keyField: Field<KEY>

    fun findAll(): Flux<POJO> =
        dslContext
            .select(*table.fields())
            .from(table)
            .let { Flux.from(it) }
            .map { it.into(type) }

    fun findById(id: KEY): Mono<POJO> =
        dslContext
            .select(*table.fields())
            .from(table)
            .where(table.field("id", keyType)!!.eq(id))
            .let { Mono.from(it) }
            .map { it.into(type) }

    fun insert(pojo: POJO): Mono<POJO> =
        dslContext
            .newRecord(table, pojo)
            .let { record ->
                dslContext
                    .insertInto(table)
                    .set(record)
                    .returning()
            }.let { Mono.from(it) }
            .map { it.into(type) }

    fun save(pojo: POJO): Mono<POJO> =
        dslContext
            .newRecord(table, pojo)
            .let { record ->
                dslContext
                    .insertInto(table)
                    .set(record)
                    .onConflict(
                        table.field("id", keyType),
                    ).doUpdate()
                    .setNonConflictingKeyToExcluded()
                    .returning()
            }.let { Mono.from(it) }
            .map { it.into(type) }

    fun saveAll(
        pojos: Flux<POJO>,
        batchSize: Int = 500,
        conflictFields: List<Field<*>> = listOf(table.field("id", keyType)!!),
    ): Flux<POJO> =
        pojos
            .buffer(batchSize)
            .filter { it.isNotEmpty() }
            .concatMap { batch ->
                val records = batch.map { dslContext.newRecord(table, it) }
                val insert = dslContext.insertInto(table).set(records)
                val nonConflictInsertColumns =
                    table.fields().filter { f -> f.name !in conflictFields.plus(keyField).map { it.name } }
                val upsert =
                    if (nonConflictInsertColumns.isEmpty()) {
                        insert
                            .onConflict()
                            .doNothing()
                    } else {
                        insert
                            .onConflict(*conflictFields.plus(keyField).toTypedArray())
                            .doUpdate()
                            .setNonConflictingKeyToExcluded()
                    }
                Flux.from(upsert.returning()).map { it.into(type) }
            }

    fun update(pojo: POJO): Mono<POJO> {
        val record = dslContext.newRecord(table, pojo)
        val idField = table.field("id", keyType)!!
        val idValue = record.get(idField)
        return dslContext
            .update(table)
            .set(record)
            .where(idField.eq(idValue))
            .returning()
            .let { Mono.from(it) }
            .map { it.into(type) }
    }

    fun delete(id: KEY) =
        Mono.from(
            dslContext
                .deleteFrom(table)
                .where(table.field("id", keyType)!!.eq(id)),
        )
}
