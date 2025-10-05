package com.moneymentor.MoneyMentor.service

import com.moneymentor.MoneyMentor.entity.User
import com.moneymentor.MoneyMentor.entity.UserStatus
import com.moneymentor.MoneyMentor.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }
    
    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
    
    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }
    
    fun existsByMobile(mobile: String): Boolean {
        return userRepository.existsByMobile(mobile)
    }
    
    fun save(user: User): User {
        return userRepository.save(user)
    }
    
    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }
    
    fun findByEmailAndStatus(email: String, status: UserStatus): User? {
        return userRepository.findByEmailAndStatus(email, status)
    }
}
