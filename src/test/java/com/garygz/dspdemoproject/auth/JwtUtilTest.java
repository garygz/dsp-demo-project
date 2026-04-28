package com.garygz.dspdemoproject.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-for-unit-test-must-be-at-least-32-chars";
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3_600_000L);
    }

    @Test
    void generate_producesNonBlankToken() {
        String token = jwtUtil.generate("user@example.com");
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_roundTrip() {
        String email = "user@example.com";
        String token = jwtUtil.generate(email);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
    }

    @Test
    void isValid_returnsTrueForFreshToken() {
        String token = jwtUtil.generate("user@example.com");
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generate("user@example.com") + "tampered";
        assertThat(jwtUtil.isValid(token)).isFalse();
    }

    @Test
    void isValid_returnsFalseForRandomString() {
        assertThat(jwtUtil.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void generate_differentEmailsProduceDifferentTokens() {
        String t1 = jwtUtil.generate("a@test.com");
        String t2 = jwtUtil.generate("b@test.com");
        assertThat(t1).isNotEqualTo(t2);
    }
}
