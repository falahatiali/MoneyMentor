package com.moneymentor.MoneyMentor.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun set(key: String, value: Any, timeout: Duration? = null) {
        redisTemplate.opsForValue().set(key, value)
        if (timeout != null) {
            redisTemplate.expire(key, timeout)
        }
    }

    fun get(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    fun delete(key: String): Boolean {
        return redisTemplate.delete(key) ?: false
    }

    fun exists(key: String): Boolean {
        return redisTemplate.hasKey(key) ?: false
    }

    fun testConnection(): String {
        return try {
            set("test:connection", "success", Duration.ofMinutes(1))
            val result = get("test:connection")
            delete("test:connection")
            "Redis connection successful: $result"
        } catch (e: Exception) {
            "Redis connection failed: ${e.message}"
        }
    }
}
