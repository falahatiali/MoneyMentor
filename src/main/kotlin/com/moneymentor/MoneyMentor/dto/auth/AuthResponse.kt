package com.moneymentor.MoneyMentor.dto.auth

import com.moneymentor.MoneyMentor.entity.Gender
import com.moneymentor.MoneyMentor.entity.UserRole
import com.moneymentor.MoneyMentor.entity.UserStatus
import java.time.LocalDateTime
import java.util.*

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto? = null
)

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val mobile: String?,
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val profileImageUrl: String?,
    val dateOfBirth: LocalDateTime?,
    val gender: Gender?,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val language: String,
    val timezone: String,
    val currency: String,
    val status: UserStatus,
    val role: UserRole,
    val twoFactorEnabled: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

