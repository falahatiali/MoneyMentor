package com.moneymentor.MoneyMentor.security

import com.moneymentor.MoneyMentor.service.JwtService
import com.moneymentor.MoneyMentor.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response)
            return
        }
        
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        val username = jwtService.extractUsername(jwt)

        if (username.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
            val user = userService.findByUsername(username)
            if (user != null && jwtService.isTokenValid(jwt, user)) {
                val authToken = UsernamePasswordAuthenticationToken(
                    user.username,
                    null,
                    user.getAuthorities()
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
    
    private fun isPublicEndpoint(requestURI: String): Boolean {
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/health/") ||
               requestURI.startsWith("/actuator/")
    }
}
