package com.moneymentor.MoneyMentor.util

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val error: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
