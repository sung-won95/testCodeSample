package com.sparta.testcode.domain.auth.controller;

import com.sparta.testcode.domain.auth.dto.LoginRequestDto;
import com.sparta.testcode.domain.auth.service.AuthService;
import com.sparta.testcode.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        String token = authService.login(requestDto);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
        return ResponseEntity.ok().build();
    }
}
