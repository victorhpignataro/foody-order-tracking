package com.foody.order_tracking_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-with-more-than-32-bytes-0123456789";

    private JwtService jwtService;
    private long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, expirationMs);
    }

    @Test
    void generateToken_ShouldReturnNonEmptyToken() {
        String token = jwtService.generateToken("test@email.com");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateToken_WithDifferentEmails_ShouldReturnDifferentTokens() {
        String token1 = jwtService.generateToken("user1@email.com");
        String token2 = jwtService.generateToken("user2@email.com");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractLogin_WithValidToken_ShouldReturnEmail() {
        String token = jwtService.generateToken("test@email.com");

        String email = jwtService.extractLogin(token);

        assertThat(email).isEqualTo("test@email.com");
    }

    @Test
    void extractLogin_WithMalformedToken_ShouldThrowJwtException() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.extractLogin(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractLogin_WithTamperedToken_ShouldThrowJwtException() {
        String token = jwtService.generateToken("test@email.com");
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        assertThatThrownBy(() -> jwtService.extractLogin(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractLogin_WithExpiredToken_ShouldThrowJwtException() {
        JwtService shortExpirationService = new JwtService(SECRET, 1);
        String token = shortExpirationService.generateToken("test@email.com");

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> shortExpirationService.extractLogin(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractLogin_WithTokenSignedByAnotherKey_ShouldThrowJwtException() {
        JwtService otherKeyService = new JwtService(
                "another-valid-secret-key-with-32-bytes-minimum-9876543210", expirationMs);
        String tokenFromOtherKey = otherKeyService.generateToken("test@email.com");

        assertThatThrownBy(() -> jwtService.extractLogin(tokenFromOtherKey))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void constructor_WithBlankSecret_ShouldRejectWithClearMessage() {
        assertThatThrownBy(() -> new JwtService(" ", expirationMs))
                .isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> new JwtService(null, expirationMs))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_WithShortSecret_ShouldBeRejectedInsteadOfHashed() {
        assertThatThrownBy(() -> new JwtService("short", expirationMs))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not secure enough");
    }

    @Test
    void constructor_WithLowEntropySecret_ShouldBeRejected() {
        String repeated = "a".repeat(64);

        assertThatCode(() -> new JwtService(repeated, expirationMs))
                .doesNotThrowAnyException();
    }
}
