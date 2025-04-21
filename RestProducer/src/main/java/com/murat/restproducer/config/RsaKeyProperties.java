package com.murat.restproducer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Configuration properties class for loading RSA public and private keys.
 *
 * <p>This record binds to properties defined under the {@code rsa} prefix
 * in the application's configuration file {@code application.properties}.</p>
 *
 * <p>Example configuration in {@code application.properties}:</p>
 * <pre>
 * rsa:
 *   public-key: classpath:certs/public.pem
 *   private-key: classpath:certs/private.pem
 * </pre>
 *
 * <p>This class is used in JWT authentication system where RSA keys are needed
 * for signing and verifying tokens.</p>
 *
 * @param publicKey  the RSA public key
 * @param privateKey the RSA private key
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see RSAPublicKey
 * @see RSAPrivateKey
 */
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {

}
