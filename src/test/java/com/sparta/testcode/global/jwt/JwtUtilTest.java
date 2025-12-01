package com.sparta.testcode.global.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "7Iqk7YyM665L7YqU64uV7J2YIOyXlO2ZlCDquZjsqK3goLgg7J207J6F64uI64ukLg==");
        jwtUtil.init();
    }

    @Test
    void createToken() {
        // given
        String username = "username";

        // when
        String token = jwtUtil.createToken(username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).startsWith(JwtUtil.BEARER_PREFIX);
    }

    @Test
    void validateToken() {
        // given
        String token = jwtUtil.createToken("username").substring(7);

        // when
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void getUserInfoFromToken() {
        // given
        String username = "username";
        String token = jwtUtil.createToken(username).substring(7);

        // when
        Claims claims = jwtUtil.getUserInfoFromToken(token);

        // then
        assertThat(claims.getSubject()).isEqualTo(username);
    }
}
