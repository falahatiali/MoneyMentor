package com.moneymentor.MoneyMentor.config

import com.moneymentor.MoneyMentor.entity.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.CustomConversions
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import java.util.*

@Configuration
class JdbcConfig : AbstractJdbcConfiguration() {

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        val converters = mutableListOf<Converter<*, *>>()
        
        // Add UUID converters
        converters.add(BinaryToUuidConverter())
        converters.add(UuidToBinaryConverter())
        
        return JdbcCustomConversions(converters)
    }

    @Bean
    fun beforeConvertCallback(): BeforeConvertCallback<User> {
        return BeforeConvertCallback { user ->
            // For Long ID, we don't need to generate anything as database will auto-increment
            user
        }
    }
}
