package com.moneymentor.MoneyMentor.repository

import com.moneymentor.MoneyMentor.entity.User
import com.moneymentor.MoneyMentor.entity.UserStatus
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Long> {
    
    fun findByEmail(email: String): User?
    
    fun findByUsername(username: String): User?
    
    fun findByMobile(mobile: String): User?
    
    fun findByEmailAndStatus(email: String, status: UserStatus): User?
    
    fun findByUsernameAndStatus(username: String, status: UserStatus): User?
    
    fun existsByEmail(email: String): Boolean
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByMobile(mobile: String): Boolean
}
