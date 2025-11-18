package com.example.codex.config

import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.using
import org.jooq.jpa.extensions.DefaultAnnotatedPojoMemberProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.ReactiveTransactionManager


@Configuration
class Beans() {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun dsl(connectionFactory: ConnectionFactory): DSLContext {
        val dsl = using(connectionFactory, SQLDialect.POSTGRES)

        // This is the crucial line:
        dsl.configuration().set(DefaultAnnotatedPojoMemberProvider())

        return dsl
    }

    @Bean
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
