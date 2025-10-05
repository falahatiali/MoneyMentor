package com.moneymentor.MoneyMentor.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.LocalDateTime
import java.util.*

@Table("users")
data class User(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("username")
    val username: String,

    @Column("email")
    val email: String,

    @Column("password")
    val password: String,

    @Column("first_name")
    val firstName: String,

    @Column("last_name")
    val lastName: String,

    @Column("phone")
    val phone: String? = null,

    @Column("mobile")
    val mobile: String? = null,

    @Column("email_verified_at")
    val emailVerifiedAt: LocalDateTime? = null,

    @Column("mobile_verified_at")
    val mobileVerifiedAt: LocalDateTime? = null,

    @Column("profile_image_url")
    val profileImageUrl: String? = null,

    @Column("date_of_birth")
    val dateOfBirth: LocalDateTime? = null,

    @Column("gender")
    val gender: Gender? = null,

    @Column("national_id")
    val nationalId: String? = null,

    @Column("address")
    val address: String? = null,

    @Column("city")
    val city: String? = null,

    @Column("state")
    val state: String? = null,

    @Column("country")
    val country: String? = null,

    @Column("postal_code")
    val postalCode: String? = null,

    @Column("language")
    val language: String = "en",

    @Column("timezone")
    val timezone: String = "UTC",

    @Column("currency")
    val currency: String = "USD",

    @Column("status")
    val status: UserStatus = UserStatus.ACTIVE,

    @Column("role")
    val role: UserRole = UserRole.USER,

    @Column("two_factor_enabled")
    val twoFactorEnabled: Boolean = false,

    @Column("two_factor_secret")
    val twoFactorSecret: String? = null,

    @Column("last_login_at")
    val lastLoginAt: LocalDateTime? = null,

    @Column("last_login_ip")
    val lastLoginIp: String? = null,

    @Column("failed_login_attempts")
    val failedLoginAttempts: Int = 0,

    @Column("locked_until")
    val lockedUntil: LocalDateTime? = null,

    @Column("password_changed_at")
    val passwordChangedAt: LocalDateTime? = null,

    @Column("terms_accepted_at")
    val termsAcceptedAt: LocalDateTime? = null,

    @Column("privacy_policy_accepted_at")
    val privacyPolicyAcceptedAt: LocalDateTime? = null,

    @Column("marketing_consent")
    val marketingConsent: Boolean = false,

    @Column("created_at")
    val createdAt: LocalDateTime? = null,

    @Column("updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column("created_by")
    val createdBy: Long? = null,

    @Column("updated_by")
    val updatedBy: Long? = null,

    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,

    @Column("deleted_by")
    val deletedBy: Long? = null
)

// Extension functions for User entity to be compatible with Spring Security's UserDetails
fun User.isEnabled(): Boolean = status == UserStatus.ACTIVE
fun User.isAccountNonExpired(): Boolean = deletedAt == null
fun User.isCredentialsNonExpired(): Boolean = true // Password never expires for now
fun User.isAccountNonLocked(): Boolean = lockedUntil?.isAfter(LocalDateTime.now()) != true
fun User.getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}

enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION, DELETED
}

enum class UserRole {
    SUPER_ADMIN, ADMIN, USER, GUEST, PREMIUM_USER, BUSINESS_USER
}