package com.example.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        try {
            Long userId = jwtService.parseUserId(token);
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                ((UsernamePasswordAuthenticationToken) authentication)
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            // Invalid token -> leave unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}
