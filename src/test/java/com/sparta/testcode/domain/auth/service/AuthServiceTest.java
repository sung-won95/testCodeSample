package com.sparta.testcode.domain.auth.service;

import com.sparta.testcode.domain.auth.dto.LoginRequestDto;
import com.sparta.testcode.domain.user.entity.User;
import com.sparta.testcode.domain.user.entity.UserRoleEnum;
import com.sparta.testcode.domain.user.repository.UserRepository;
import com.sparta.testcode.global.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_Success() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "비밀번호123");

        User user = User.create("테스트유저", "암호화된비밀번호", UserRoleEnum.USER);
        given(userRepository.findByUsername("테스트유저")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("비밀번호123", "암호화된비밀번호")).willReturn(true);
        given(jwtUtil.createToken("테스트유저")).willReturn("token");

        // when
        String token = authService.login(requestDto);

        // then
        assertThat(token).isEqualTo("token");
    }

    @Test
    void login_UserNotFound() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "비밀번호123");

        given(userRepository.findByUsername("테스트유저")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }

    @Test
    void login_PasswordMismatch() {
        // given
        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "비밀번호123");

        User user = User.create("테스트유저", "암호화된비밀번호", UserRoleEnum.USER);
        given(userRepository.findByUsername("테스트유저")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("비밀번호123", "암호화된비밀번호")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호 오류");
    }
}
