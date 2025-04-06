package com.murat.restproducer.controller;

import com.murat.restproducer.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public String createToken(Authentication authentication) {

        logger.info("Creating new token for user: {}", authentication.getName());
        String token = tokenService.generateToken(authentication);
        logger.info("Generated token: {}", token);

        return token;
    }
}
