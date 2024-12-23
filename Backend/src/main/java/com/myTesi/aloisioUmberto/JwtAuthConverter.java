package com.myTesi.aloisioUmberto;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JwtAuthConverter is a component used to extract authentication details
 * and roles from a JWT. This class implements a Converter interface
 * to convert JWT objects into Spring Security authentication tokens.
 */
@Component
public final class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    private final ThreadLocal<String> emailThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<String> idThreadLocal = new ThreadLocal<>();

    /**
     * Extracts the principal claim name from the JWT. If a custom principle attribute is configured,
     * it uses that; otherwise, it defaults to the "sub" claim.
     *
     * @param jwt The JWT from which the principal claim is extracted.
     * @return The principal claim value.
     */
    private String getPrincipleClaimName(Jwt jwt) {
        setEmail(jwt);
        setId(jwt);
        String claimName = JwtClaimNames.SUB;
        if (principleAttribute != null) {
            claimName = principleAttribute;
        }
        return jwt.getClaim(claimName);
    }

    /**
     * Extracts roles from the "resource_access" section of the JWT.
     * Only roles for the configured resource ID are returned.
     *
     * @param jwt The JWT containing resource access information.
     * @return A collection of GrantedAuthority representing the roles.
     */
    public Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Set.of();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
        if (resource == null) {
            return Set.of();
        }

        Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
        if (resourceRoles == null) {
            return Set.of();
        }

        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    /**
     * Stores the email claim from the JWT in a ThreadLocal variable.
     *
     * @param jwt The JWT containing the email claim.
     */
    private void setEmail(Jwt jwt) {
        emailThreadLocal.set(jwt.getClaim("email"));
    }

    /**
     * Retrieves the stored email value from the ThreadLocal variable.
     *
     * @return The email value.
     */
    public String getEmail() {
        return emailThreadLocal.get();
    }

    /**
     * Stores the Keycloak ID ("sub" claim) from the JWT in a ThreadLocal variable.
     *
     * @param jwt The JWT containing the "sub" claim.
     */
    private void setId(Jwt jwt) {
        idThreadLocal.set(jwt.getClaim("sub"));
    }

    /**
     * Retrieves the stored Keycloak ID value from the ThreadLocal variable.
     *
     * @return The Keycloak ID value.
     */
    public String getId() {
        return idThreadLocal.get();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return null;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return null;
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return null;
    }
}
