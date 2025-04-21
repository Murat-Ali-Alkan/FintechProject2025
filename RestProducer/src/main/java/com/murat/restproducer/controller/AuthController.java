package com.murat.restproducer.controller;

import com.murat.restproducer.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for generating JWT tokens for authenticated users.
 *
 * <p>This controller exposes a POST endpoint to create a new JWT token for a user.
 * It uses the {@link TokenService} to generate the token based on the user's authentication information.</p>
 *
 * @see TokenService
 */
@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final TokenService tokenService;

    /**
     * Constructs a new {@code AuthController} with the given {@link TokenService}.
     *
     * @param tokenService the service used to generate JWT tokens
     */
    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Creates a new JWT token for the authenticated user.
     *
     * <p>This method is called when a user sends a POST request to the {@code /token} endpoint.
     * The {@link Authentication} object, which contains the user's details, is automatically injected by Spring Security.</p>
     *
     * @param authentication the authentication object containing the user's details
     * @return a newly generated JWT token as a string
     */
    @PostMapping("/token")
    public String createToken(Authentication authentication) {

        logger.info("Creating new token for user: {}", authentication.getName());
        String token = tokenService.generateToken(authentication);
        logger.info("Generated token: {}", token);

        return token;
    }
}
