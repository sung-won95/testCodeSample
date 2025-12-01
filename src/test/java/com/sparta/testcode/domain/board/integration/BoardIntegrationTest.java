package com.sparta.testcode.domain.board.integration;

import com.sparta.testcode.domain.auth.dto.LoginRequestDto;
import com.sparta.testcode.domain.board.dto.BoardRequestDto;
import com.sparta.testcode.domain.board.dto.BoardResponseDto;
import com.sparta.testcode.domain.board.entity.Board;
import com.sparta.testcode.domain.board.repository.BoardRepository;
import com.sparta.testcode.domain.user.entity.User;
import com.sparta.testcode.domain.user.entity.UserRoleEnum;
import com.sparta.testcode.domain.user.repository.UserRepository;
import com.sparta.testcode.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BoardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() {
        boardRepository.deleteAll();
        userRepository.deleteAll();

        // Create User
        String password = passwordEncoder.encode("비밀번호123");
        User user = User.create("테스트유저", password, UserRoleEnum.USER);
        userRepository.save(user);

        // Login to get Token
        LoginRequestDto loginRequest = new LoginRequestDto("테스트유저", "비밀번호123");
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/login", loginRequest, Void.class);
        token = response.getHeaders().getFirst(JwtUtil.AUTHORIZATION_HEADER);
    }

    @Test
    void createBoard() {
        // given
        BoardRequestDto requestDto = new BoardRequestDto("제목", "내용", "작성자");
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtUtil.AUTHORIZATION_HEADER, token);
        HttpEntity<BoardRequestDto> request = new HttpEntity<>(requestDto, headers);

        // when
        ResponseEntity<BoardResponseDto> response = restTemplate.postForEntity("/api/boards", request, BoardResponseDto.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("제목");
        assertThat(response.getBody().getContent()).isEqualTo("내용");
        assertThat(response.getBody().getAuthor()).isEqualTo("작성자");

        // Verify DB
        assertThat(boardRepository.findAll()).hasSize(1);
    }

    @Test
    void getBoard() {
        // given
        Board board = Board.create("제목", "내용", "작성자");
        Board savedBoard = boardRepository.save(board);

        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtUtil.AUTHORIZATION_HEADER, token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<BoardResponseDto> response = restTemplate.exchange(
                "/api/boards/" + savedBoard.getId(),
                HttpMethod.GET,
                request,
                BoardResponseDto.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("제목");
    }

    @Test
    void getAllBoards() {
        // given
        boardRepository.save(Board.create("제목1", "내용1", "작성자1"));
        boardRepository.save(Board.create("제목2", "내용2", "작성자2"));

        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtUtil.AUTHORIZATION_HEADER, token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<BoardResponseDto[]> response = restTemplate.exchange(
                "/api/boards",
                HttpMethod.GET,
                request,
                BoardResponseDto[].class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void updateBoard() {
        // given
        Board board = Board.create("제목", "내용", "작성자");
        Board savedBoard = boardRepository.save(board);

        BoardRequestDto requestDto = new BoardRequestDto("수정된 제목", "수정된 내용", null);
        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtUtil.AUTHORIZATION_HEADER, token);
        HttpEntity<BoardRequestDto> request = new HttpEntity<>(requestDto, headers);

        // when
        ResponseEntity<BoardResponseDto> response = restTemplate.exchange(
                "/api/boards/" + savedBoard.getId(),
                HttpMethod.PUT,
                request,
                BoardResponseDto.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getBody().getContent()).isEqualTo("수정된 내용");

        // Verify DB
        Board updatedBoard = boardRepository.findById(savedBoard.getId()).orElseThrow();
        assertThat(updatedBoard.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    void deleteBoard() {
        // given
        Board board = Board.create("제목", "내용", "작성자");
        Board savedBoard = boardRepository.save(board);

        HttpHeaders headers = new HttpHeaders();
        headers.set(JwtUtil.AUTHORIZATION_HEADER, token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // when
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/boards/" + savedBoard.getId(),
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify DB
        assertThat(boardRepository.existsById(savedBoard.getId())).isFalse();
    }
}
