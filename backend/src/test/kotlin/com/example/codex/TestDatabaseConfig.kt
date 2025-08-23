package com.example.codex

import org.mockito.Mockito
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.sql.Connection
import javax.sql.DataSource

object SchemaHolder {
    val current: ThreadLocal<String> = ThreadLocal()
}

@TestConfiguration
class TestDatabaseConfig {
    @Bean
    fun dataSourceSpyPostProcessor(): BeanPostProcessor = object : BeanPostProcessor {
        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
            if (bean is DataSource) {
                val spy = Mockito.spy(bean)
                Mockito.doAnswer { invocation ->
                    val connection = invocation.callRealMethod() as Connection
                    SchemaHolder.current.get()?.let { schema ->
                        connection.createStatement().use { stmt ->
                            stmt.execute("set search_path to \"$schema\"")
                        }
                    }
                    connection
                }.`when`(spy).connection
                Mockito.doAnswer { invocation ->
                    val connection = invocation.callRealMethod() as Connection
                    SchemaHolder.current.get()?.let { schema ->
                        connection.createStatement().use { stmt ->
                            stmt.execute("set search_path to \"$schema\"")
                        }
                    }
                    connection
                }.`when`(spy).getConnection(Mockito.anyString(), Mockito.anyString())
                return spy
            }
            return bean
        }
    }
}

