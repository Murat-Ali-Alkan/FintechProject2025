package com.murat.restproducer.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating JWT tokens for authenticated users.
 *
 * <p>This class uses Spring Security's {@link JwtEncoder} to create secure JWTs
 * based on the authenticated user's information and authorities (roles/permissions).</p>
 *
 * @see JwtEncoder
 * @see Authentication
 */
@Service
public class TokenService {
    private final JwtEncoder encoder;


    /**
     * Constructs a {@code TokenService} with the specified {@link JwtEncoder}.
     *
     * @param encoder the JWT encoder used to encode token claims
     */
    public TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }


    /**
     * Generates a signed JWT token for the given authenticated user.
     *
     * @param auth the {@link Authentication} object containing the user's credentials and authorities
     * @return a signed JWT as a string
     */
    public String generateToken(Authentication auth) {
        Instant now = Instant.now();

        // Create a space-separated string of the user's authorities
        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        // Create the JWT claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(auth.getName())
                .claim("scope",scope)
                .build();

        // Encode and return the token
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
