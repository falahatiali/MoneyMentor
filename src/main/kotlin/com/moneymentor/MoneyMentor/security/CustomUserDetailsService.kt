package com.moneymentor.MoneyMentor.security

import com.moneymentor.MoneyMentor.entity.User
import com.moneymentor.MoneyMentor.entity.UserRole
import com.moneymentor.MoneyMentor.entity.UserStatus
import com.moneymentor.MoneyMentor.service.UserService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomUserDetailsService(
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            user.isEnabled(),
            user.isAccountNonExpired(),
            user.isCredentialsNonExpired(),
            user.isAccountNonLocked(),
            user.getAuthorities()
        )
    }
}

// Extension functions for User entity
fun User.isEnabled(): Boolean = status == UserStatus.ACTIVE

fun User.isAccountNonExpired(): Boolean = true

fun User.isCredentialsNonExpired(): Boolean = true

fun User.isAccountNonLocked(): Boolean = status != UserStatus.SUSPENDED && (lockedUntil?.isBefore(LocalDateTime.now()) != false)

fun User.getAuthorities(): Collection<GrantedAuthority> {
    return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
}
