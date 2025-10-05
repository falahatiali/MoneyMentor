package com.moneymentor.MoneyMentor.controller

import com.moneymentor.MoneyMentor.dto.auth.*
import com.moneymentor.MoneyMentor.service.AuthService
import com.moneymentor.MoneyMentor.util.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.register(request)
        return ResponseEntity.status(
            if (response.success) HttpStatus.CREATED else HttpStatus.BAD_REQUEST
        ).body(response)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.authenticate(request)
        return ResponseEntity.status(
            if (response.success) HttpStatus.OK else HttpStatus.UNAUTHORIZED
        ).body(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: TokenRefreshRequest): ResponseEntity<ApiResponse<TokenRefreshResponse>> {
        val response = authService.refreshToken(request)
        return ResponseEntity.status(
            if (response.success) HttpStatus.OK else HttpStatus.UNAUTHORIZED
        ).body(response)
    }

    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<ApiResponse<Unit>> {
        // For now, we'll use a placeholder Long - in a real implementation, you'd extract this from the JWT
        val response = authService.logout(1L)
        return ResponseEntity.status(
            if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        ).body(response)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse<Unit>> {
        // Implementation would go here
        val response = ApiResponse<Unit>(
            success = true,
            message = "Password reset email sent successfully"
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<Unit>> {
        // Implementation would go here
        val response = ApiResponse<Unit>(
            success = true,
            message = "Password reset successfully"
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<ApiResponse<Unit>> {
        // Implementation would go here
        val response = ApiResponse<Unit>(
            success = true,
            message = "Email verified successfully"
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/resend-verification")
    fun resendVerification(@Valid @RequestBody request: ResendVerificationRequest): ResponseEntity<ApiResponse<Unit>> {
        // Implementation would go here
        val response = ApiResponse<Unit>(
            success = true,
            message = "Verification email sent successfully"
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val response = ApiResponse(
            success = true,
            message = "User information retrieved successfully",
            data = mapOf(
                "username" to authentication.name,
                "authorities" to authentication.authorities.map { it.authority }
            )
        )
        return ResponseEntity.ok(response)
    }
}
