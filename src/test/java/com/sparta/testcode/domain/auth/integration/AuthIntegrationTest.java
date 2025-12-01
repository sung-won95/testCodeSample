package com.sparta.testcode.domain.auth.integration;

import com.sparta.testcode.domain.auth.dto.LoginRequestDto;
import com.sparta.testcode.domain.user.entity.User;
import com.sparta.testcode.domain.user.entity.UserRoleEnum;
import com.sparta.testcode.domain.user.repository.UserRepository;
import com.sparta.testcode.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        String password = passwordEncoder.encode("비밀번호123");
        User user = User.create("테스트유저", password, UserRoleEnum.USER);
        userRepository.save(user);
    }

    @Test
    void login_Success() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "비밀번호123");

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/login", requestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(JwtUtil.AUTHORIZATION_HEADER)).isNotNull();
        assertThat(response.getHeaders().getFirst(JwtUtil.AUTHORIZATION_HEADER)).startsWith("Bearer ");
    }

    @Test
    void login_Failure_UserNotFound() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("존재하지않는유저", "비밀번호123");

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/login", requestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_Failure_WrongPassword() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "틀린비밀번호");

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/login", requestDto, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
