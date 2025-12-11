package com.sparta.testcode.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.testcode.domain.auth.dto.LoginRequestDto;
import com.sparta.testcode.domain.auth.service.AuthService;
import com.sparta.testcode.global.config.SecurityConfig;
import com.sparta.testcode.global.jwt.JwtUtil;
import com.sparta.testcode.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void login() throws Exception {
        // given

        LoginRequestDto requestDto = new LoginRequestDto("테스트유저", "비밀번호123");

        String token = "Bearer token";
        given(authService.login(any(LoginRequestDto.class))).willReturn(token);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(JwtUtil.AUTHORIZATION_HEADER, token));
    }
}
