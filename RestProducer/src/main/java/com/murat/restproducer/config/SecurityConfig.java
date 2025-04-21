package com.murat.restproducer.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Spring Security configuration class that sets up JWT-based stateless authentication
 * using RSA public/private keys.
 *
 * <p>This configuration:</p>
 * <ul>
 *   <li>Disables CSRF protection</li>
 *   <li>Requires authentication for all endpoints</li>
 *   <li>Enables HTTP Basic authentication</li>
 *   <li>Configures the application as an OAuth2 Resource Server using JWT</li>
 *   <li>Uses RSA keys (provided by {@link RsaKeyProperties}) for signing and verifying JWTs</li>
 * </ul>
 *
 * <p>Also defines an in-memory user with username {@code admin} and password {@code admin}, with "read" authority.</p>
 *
 * @see RsaKeyProperties
 * @see NimbusJwtDecoder
 * @see NimbusJwtEncoder
 * @see SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RsaKeyProperties rsaKeyProperties;

    /**
     * Constructs a new SecurityConfig instance with the given RSA key properties.
     * Dependency injection is used
     * @param rsaKeyProperties contains the RSA public/private keys for JWT encoding and decoding
     */
    public SecurityConfig(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }

    /**
     * Defines a simple in-memory user for authentication.
     *
     * @return an {@link InMemoryUserDetailsManager} with a single user
     */
    @Bean
    public InMemoryUserDetailsManager user() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin")
                        .password("{noop}admin")
                        .authorities("read")
                        .build()
        );
    }

    /**
     * Configures the application's HTTP security using a stateless, JWT-based authentication model.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the {@link SecurityFilterChain} to be applied
     * @throws Exception in case of configuration errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth ->
                                auth.anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Configures the JWT decoder using the RSA public key.
     *
     * @return a {@link JwtDecoder} that can validate JWTs signed with the corresponding private key
     */
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }

    /**
     * Configures the JWT encoder using the RSA key pair.
     *
     * @return a {@link JwtEncoder} that signs JWTs using the private key
     */
    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeyProperties.publicKey()).privateKey(rsaKeyProperties.privateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
}
