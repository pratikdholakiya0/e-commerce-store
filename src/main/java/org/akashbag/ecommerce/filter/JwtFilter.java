package org.akashbag.ecommerce.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.repository.JwtTokenRepository;
import org.akashbag.ecommerce.service.UserDetailServiceImpl;
import org.akashbag.ecommerce.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailServiceImpl userDetailsService;
    private final JwtTokenRepository jwtTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            // 1. Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                jwtTokenRepository.deleteByToken(token);
                handleException(response, "Token has expired", HttpStatus.UNAUTHORIZED);
                return; // Stop the chain
            }

            // 2. Critical: Check if token exists in DB (Revocation Check)
            boolean isPresent = jwtTokenRepository.existsByToken(token);
            if (!isPresent) {
                // Throwing error manually because this token is revoked/invalid
                handleException(response, "This token is invalid or has been logged out. It can't be used anymore.", HttpStatus.UNAUTHORIZED);
                return; // Stop the chain
            }

            // 3. Authenticate if username is present and context is empty
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("JWT Processing failed: {}", e);
            handleException(response, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Helper method to send a clean JSON error response back to the client
     */
    private void handleException(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        // Creating a simple JSON structure
        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\"}",
                java.time.LocalDateTime.now(), status.value(), message
        );

        response.getWriter().write(jsonResponse);
    }
}