package com.moneymentor.MoneyMentor.service

import com.moneymentor.MoneyMentor.dto.auth.*
import com.moneymentor.MoneyMentor.entity.*
import com.moneymentor.MoneyMentor.util.ApiResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
// @Transactional // Temporarily disabled for debugging
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val redisService: RedisService,
    private val emailService: EmailService?
) {

    fun register(request: RegisterRequest): ApiResponse<AuthResponse> {
        return try {
            // Check if user already exists
            if (userService.existsByEmail(request.email)) {
                return ApiResponse(
                    success = false,
                    message = "User with this email already exists"
                )
            }

            if (userService.existsByUsername(request.username)) {
                return ApiResponse(
                    success = false,
                    message = "Username already taken"
                )
            }

            // Create new user with default values for optional fields
            val user = User(
                id = null, // Let database auto-generate ID
                username = request.username,
                email = request.email,
                password = passwordEncoder.encode(request.password),
                firstName = request.firstName ?: "",
                lastName = request.lastName ?: "",
                phone = request.phone,
                mobile = request.mobile,
                dateOfBirth = request.dateOfBirth?.let { LocalDateTime.parse(it) },
                gender = request.gender?.let { Gender.valueOf(it.uppercase()) },
                country = request.country,
                language = request.language ?: "en",
                timezone = request.timezone ?: "UTC",
                currency = request.currency ?: "USD",
                status = UserStatus.ACTIVE, // Changed to ACTIVE since we don't have email verification working yet
                termsAcceptedAt = if (request.termsAccepted) LocalDateTime.now() else null,
                privacyPolicyAcceptedAt = if (request.privacyPolicyAccepted) LocalDateTime.now() else null,
                marketingConsent = request.marketingConsent
            )

            val savedUser = userService.save(user)

            // Send verification email if email service is available
            emailService?.sendVerificationEmail(savedUser)

            ApiResponse(
                success = true,
                message = "User registered successfully. Please check your email for verification.",
                data = createAuthResponse(savedUser)
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "Registration failed: ${e.message}"
            )
        }
    }

    fun authenticate(request: AuthRequest): ApiResponse<AuthResponse> {
        return try {
            // Check if user is locked
            val user = findUserByUsernameOrEmail(request.identifier)
                ?: throw UsernameNotFoundException("User not found")

            if (user.status != UserStatus.ACTIVE) {
                return ApiResponse(
                    success = false,
                    message = "Account is not active. Please verify your email or contact support."
                )
            }

            if (isUserLocked(user)) {
                return ApiResponse(
                    success = false,
                    message = "Account is temporarily locked due to multiple failed login attempts."
                )
            }

            // Authenticate user
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    request.identifier,
                    request.password
                )
            )

            // Reset failed login attempts
            val updatedUser = user.copy(
                failedLoginAttempts = 0,
                lockedUntil = null,
                lastLoginAt = LocalDateTime.now(),
                lastLoginIp = request.ipAddress
            )
            userService.save(updatedUser)

            // Generate tokens
            val authResponse = createAuthResponse(updatedUser)

            // Store refresh token in Redis
            val refreshToken = jwtService.generateRefreshToken(updatedUser)
            redisService.set(
                "refresh_token:${updatedUser.id}",
                refreshToken,
                java.time.Duration.ofDays(7)
            )

            ApiResponse(
                success = true,
                message = "Login successful",
                data = authResponse.copy(refreshToken = refreshToken)
            )
        } catch (e: BadCredentialsException) {
            handleFailedLogin(request.identifier)
            ApiResponse(
                success = false,
                message = "Invalid credentials"
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "Login failed: ${e.message}"
            )
        }
    }

    fun refreshToken(request: TokenRefreshRequest): ApiResponse<TokenRefreshResponse> {
        return try {
            val refreshToken = request.refreshToken
            val username = jwtService.extractUsername(refreshToken)
            val userId = jwtService.extractUserId(refreshToken)

            if (!jwtService.isRefreshToken(refreshToken)) {
                return ApiResponse(
                    success = false,
                    message = "Invalid token type"
                )
            }

            // Verify refresh token in Redis
            val storedToken = redisService.get("refresh_token:$userId") as? String
            if (storedToken != refreshToken) {
                return ApiResponse(
                    success = false,
                    message = "Invalid refresh token"
                )
            }

        val user = userService.findById(userId)
            ?: throw UsernameNotFoundException("User not found")

            if (user.status != UserStatus.ACTIVE) {
                return ApiResponse(
                    success = false,
                    message = "Account is not active"
                )
            }

            val newAccessToken = jwtService.generateToken(user)
            val newRefreshToken = jwtService.generateRefreshToken(user)

            // Update refresh token in Redis
            redisService.set(
                "refresh_token:$userId",
                newRefreshToken,
                java.time.Duration.ofDays(7)
            )

            ApiResponse(
                success = true,
                message = "Token refreshed successfully",
                data = TokenRefreshResponse(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    expiresIn = jwtService.getTokenExpirationSeconds()
                )
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "Token refresh failed: ${e.message}"
            )
        }
    }

    fun logout(userId: Long): ApiResponse<Unit> {
        return try {
            // Remove refresh token from Redis
            redisService.delete("refresh_token:$userId")
            
            ApiResponse(
                success = true,
                message = "Logout successful"
            )
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "Logout failed: ${e.message}"
            )
        }
    }

    private fun findUserByUsernameOrEmail(usernameOrEmail: String): User? {
        return if (usernameOrEmail.contains("@")) {
            userService.findByEmail(usernameOrEmail)
        } else {
            userService.findByUsername(usernameOrEmail)
        }
    }

    private fun isUserLocked(user: User): Boolean {
        return user.lockedUntil?.isAfter(LocalDateTime.now()) == true
    }

    private fun handleFailedLogin(usernameOrEmail: String) {
        val user = findUserByUsernameOrEmail(usernameOrEmail) ?: return
        
        val failedAttempts = user.failedLoginAttempts + 1
        val lockedUntil = if (failedAttempts >= 5) {
            LocalDateTime.now().plusMinutes(30) // Lock for 30 minutes
        } else null

        val updatedUser = user.copy(
            failedLoginAttempts = failedAttempts,
            lockedUntil = lockedUntil
        )
        userService.save(updatedUser)
    }

    private fun createAuthResponse(user: User): AuthResponse {
        return AuthResponse(
            accessToken = jwtService.generateToken(user),
            refreshToken = "", // Will be set separately for login
            expiresIn = jwtService.getTokenExpirationSeconds(),
            user = UserDto(
                id = user.id!!,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                phone = user.phone,
                mobile = user.mobile,
                emailVerified = user.emailVerifiedAt != null,
                mobileVerified = user.mobileVerifiedAt != null,
                profileImageUrl = user.profileImageUrl,
                dateOfBirth = user.dateOfBirth,
                gender = user.gender,
                address = user.address,
                city = user.city,
                state = user.state,
                country = user.country,
                language = user.language,
                timezone = user.timezone,
                currency = user.currency,
                status = user.status,
                role = user.role,
                twoFactorEnabled = user.twoFactorEnabled,
                lastLoginAt = user.lastLoginAt,
                createdAt = user.createdAt ?: LocalDateTime.now()
            )
        )
    }
}
