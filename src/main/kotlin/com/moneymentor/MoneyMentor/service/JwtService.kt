package com.moneymentor.MoneyMentor.service

import com.moneymentor.MoneyMentor.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function
import kotlin.collections.HashMap

@Service
class JwtService {

    @Value("\${jwt.secret:mySecretKey}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration:86400000}") // 24 hours
    private var jwtExpiration: Long = 0

    @Value("\${jwt.refresh-expiration:604800000}") // 7 days
    private var refreshExpiration: Long = 0

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractUserId(token: String): Long {
        val userId = extractClaim(token) { claims -> claims["userId"] }
        return userId.toString().toLong()
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    fun generateToken(user: User): String {
        return generateToken(HashMap(), user)
    }

    fun generateRefreshToken(user: User): String {
        val extraClaims = HashMap<String, Any>()
        extraClaims["type"] = "refresh"
        return buildToken(extraClaims, user, refreshExpiration)
    }

    fun generateToken(extraClaims: Map<String, Any>, user: User): String {
        return buildToken(extraClaims, user, jwtExpiration)
    }

    private fun buildToken(
        extraClaims: Map<String, Any>,
        user: User,
        expiration: Long
    ): String {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(user.username)
            .claim("userId", user.id.toString())
            .claim("email", user.email)
            .claim("role", user.role.name)
            .claim("status", user.status.name)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token: String, user: User): Boolean {
        val username = extractUsername(token)
        return username == user.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun getSignInKey(): Key {
        val keyBytes = secretKey.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun extractExpirationTime(token: String): Long {
        return extractExpiration(token).time
    }

    fun isRefreshToken(token: String): Boolean {
        val claims = extractAllClaims(token)
        return claims["type"] == "refresh"
    }

    fun getTokenExpirationSeconds(): Long {
        return jwtExpiration / 1000
    }

    fun getRefreshTokenExpirationSeconds(): Long {
        return refreshExpiration / 1000
    }
}
