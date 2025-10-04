package com.moneymentor.MoneyMentor.controller

import com.moneymentor.MoneyMentor.service.RedisService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@RestController
@RequestMapping("/api/health")
class HealthController(
    private val dataSource: DataSource,
    private val redisService: RedisService
) {

    @GetMapping("/database")
    fun checkDatabase(): Map<String, Any> {
        return try {
            dataSource.connection.use { connection ->
                val databaseName = connection.metaData.databaseProductName ?: "Unknown"
                mapOf(
                    "status" to "UP",
                    "message" to "Database connection successful",
                    "database" to databaseName
                )
            }
        } catch (e: Exception) {
            mapOf(
                "status" to "DOWN",
                "message" to "Database connection failed",
                "error" to (e.message ?: "Unknown error")
            )
        }
    }

    @GetMapping("/redis")
    fun checkRedis(): Map<String, Any> {
        return try {
            val result = redisService.testConnection()
            mapOf(
                "status" to "UP",
                "message" to result
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "DOWN",
                "message" to "Redis connection failed",
                "error" to (e.message ?: "Unknown error")
            )
        }
    }

    @GetMapping("/all")
    fun checkAll(): Map<String, Any> {
        return mapOf(
            "database" to checkDatabase(),
            "redis" to checkRedis(),
            "timestamp" to System.currentTimeMillis()
        )
    }
}
