package com.moneymentor.MoneyMentor.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthRequest(
    @field:NotBlank(message = "Username or email is required")
    val identifier: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    val rememberMe: Boolean = false,
    val deviceInfo: String? = null,
    val ipAddress: String? = null
)

data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    // Optional fields
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val mobile: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val country: String? = null,
    val language: String? = "en",
    val timezone: String? = "UTC",
    val currency: String? = "USD",
    val termsAccepted: Boolean = false,
    val privacyPolicyAccepted: Boolean = false,
    val marketingConsent: Boolean = false
)

data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Token is required")
    val token: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

data class VerifyEmailRequest(
    @field:NotBlank(message = "Token is required")
    val token: String
)

data class ResendVerificationRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String
)
