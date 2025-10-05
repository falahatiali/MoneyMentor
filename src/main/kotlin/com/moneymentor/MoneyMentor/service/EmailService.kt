package com.moneymentor.MoneyMentor.service

import com.moneymentor.MoneyMentor.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.*

@Service
@ConditionalOnProperty(name = ["spring.mail.host"], matchIfMissing = false)
class EmailService(
    private val mailSender: JavaMailSender,
    private val redisService: RedisService
) {

    @Value("\${app.mail.from:noreply@moneymentor.com}")
    private lateinit var fromEmail: String

    @Value("\${app.frontend.url:http://localhost:3000}")
    private lateinit var frontendUrl: String

    fun sendVerificationEmail(user: User) {
        val token = generateVerificationToken()
        
        // Store token in Redis for 24 hours
        redisService.set(
            "email_verification:$token",
            user.id.toString(),
            java.time.Duration.ofHours(24)
        )

        val subject = "Verify Your Email - MoneyMentor"
        val verificationUrl = "$frontendUrl/verify-email?token=$token"
        
        val message = """
            Hello ${user.firstName},
            
            Welcome to MoneyMentor! Please click the link below to verify your email address:
            
            $verificationUrl
            
            This link will expire in 24 hours.
            
            If you didn't create an account with us, please ignore this email.
            
            Best regards,
            The MoneyMentor Team
        """.trimIndent()

        sendEmail(user.email, subject, message)
    }

    fun sendPasswordResetEmail(user: User) {
        val token = generatePasswordResetToken()
        
        // Store token in Redis for 1 hour
        redisService.set(
            "password_reset:$token",
            user.id.toString(),
            java.time.Duration.ofHours(1)
        )

        val subject = "Reset Your Password - MoneyMentor"
        val resetUrl = "$frontendUrl/reset-password?token=$token"
        
        val message = """
            Hello ${user.firstName},
            
            You requested to reset your password. Please click the link below to reset it:
            
            $resetUrl
            
            This link will expire in 1 hour.
            
            If you didn't request this password reset, please ignore this email.
            
            Best regards,
            The MoneyMentor Team
        """.trimIndent()

        sendEmail(user.email, subject, message)
    }

    fun sendWelcomeEmail(user: User) {
        val subject = "Welcome to MoneyMentor!"
        
        val message = """
            Hello ${user.firstName},
            
            Welcome to MoneyMentor! Your email has been successfully verified.
            
            You can now start managing your finances with our powerful tools:
            - Track your income and expenses
            - Set up budgets
            - Generate detailed reports
            - And much more!
            
            Get started by logging in to your account.
            
            Best regards,
            The MoneyMentor Team
        """.trimIndent()

        sendEmail(user.email, subject, message)
    }

    fun sendAccountLockedEmail(user: User) {
        val subject = "Account Security Alert - MoneyMentor"
        
        val message = """
            Hello ${user.firstName},
            
            Your account has been temporarily locked due to multiple failed login attempts.
            
            This is a security measure to protect your account. Your account will be automatically unlocked after 30 minutes.
            
            If you believe this was an unauthorized attempt, please contact our support team immediately.
            
            Best regards,
            The MoneyMentor Team
        """.trimIndent()

        sendEmail(user.email, subject, message)
    }

    fun verifyEmailToken(token: String): UUID? {
        val value = redisService.get("email_verification:$token") as? String
        return value?.let { UUID.fromString(it) }
    }

    fun verifyPasswordResetToken(token: String): UUID? {
        val value = redisService.get("password_reset:$token") as? String
        return value?.let { UUID.fromString(it) }
    }

    fun removeEmailVerificationToken(token: String) {
        redisService.delete("email_verification:$token")
    }

    fun removePasswordResetToken(token: String) {
        redisService.delete("password_reset:$token")
    }

    private fun sendEmail(to: String, subject: String, message: String) {
        try {
            val mailMessage = SimpleMailMessage().apply {
                setFrom(fromEmail)
                setTo(to)
                setSubject(subject)
                setText(message)
            }
            
            mailSender.send(mailMessage)
        } catch (e: Exception) {
            // Log error but don't throw exception to avoid breaking the flow
            println("Failed to send email to $to: ${e.message}")
        }
    }

    private fun generateVerificationToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    private fun generatePasswordResetToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
