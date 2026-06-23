package com.myTesi.aloisioUmberto.core.security;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromUserToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // DEBUG: Guarda il terminale quando fai la chiamata!
            System.out.println("JWT DEBUG: Email=" + email + " | Role=" + role);

            if (email != null && role != null) {
                // Pulizia del ruolo: se il ruolo contiene già "ROLE_", lo usiamo così com'è,
                // altrimenti aggiungiamo il prefisso.
                String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                System.out.println("JWT DEBUG: Formatted Role=" + formattedRole);

                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(formattedRole));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}