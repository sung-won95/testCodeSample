package com.sparta.testcode.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.testcode.domain.board.dto.BoardRequestDto;
import com.sparta.testcode.domain.board.dto.BoardResponseDto;
import com.sparta.testcode.domain.board.entity.Board;
import com.sparta.testcode.domain.board.service.BoardService;
import com.sparta.testcode.global.config.SecurityConfig;
import com.sparta.testcode.global.jwt.JwtUtil;
import com.sparta.testcode.global.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BoardController.class)
@Import(SecurityConfig.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList())
        );
    }

    @Test
    void createBoard() throws Exception {
        // given
        BoardRequestDto requestDto = new BoardRequestDto("제목", "내용", "작성자");

        Board board = Board.create("제목", "내용", "작성자");
        BoardResponseDto responseDto = new BoardResponseDto(board);

        given(boardService.createBoard(any(BoardRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("내용"))
                .andExpect(jsonPath("$.author").value("작성자"));
    }

    @Test
    void createBoard_Unauthenticated() throws Exception {
        // given
        SecurityContextHolder.clearContext();
        BoardRequestDto requestDto = new BoardRequestDto("제목", "내용", "작성자");

        // when & then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isForbidden()); // or isUnauthorized depending on config
    }

    @Test
    void getBoard() throws Exception {
        // given
        Long boardId = 1L;
        Board board = Board.create("제목", "내용", "작성자");
        BoardResponseDto responseDto = new BoardResponseDto(board);

        given(boardService.getBoard(boardId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/boards/{id}", boardId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    void getAllBoards() throws Exception {
        // given
        Board board1 = Board.create("제목1", "내용1", "작성자1");
        Board board2 = Board.create("제목2", "내용2", "작성자2");
        List<BoardResponseDto> responseDtos = List.of(new BoardResponseDto(board1), new BoardResponseDto(board2));

        given(boardService.getAllBoards()).willReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/api/boards"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("제목1"));
    }

    @Test
    void updateBoard() throws Exception {
        // given
        Long boardId = 1L;
        BoardRequestDto requestDto = new BoardRequestDto("수정된 제목", "수정된 내용", null);

        Board board = Board.create("수정된 제목", "수정된 내용", "작성자");
        BoardResponseDto responseDto = new BoardResponseDto(board);

        given(boardService.updateBoard(any(Long.class), any(BoardRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/boards/{id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"));
    }

    @Test
    void updateBoard_Unauthenticated() throws Exception {
        // given
        SecurityContextHolder.clearContext();
        Long boardId = 1L;
        BoardRequestDto requestDto = new BoardRequestDto("수정된 제목", "수정된 내용", null);

        // when & then
        mockMvc.perform(put("/api/boards/{id}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBoard() throws Exception {
        // given
        Long boardId = 1L;

        // when & then
        mockMvc.perform(delete("/api/boards/{id}", boardId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBoard_Unauthenticated() throws Exception {
        // given
        SecurityContextHolder.clearContext();
        Long boardId = 1L;

        // when & then
        mockMvc.perform(delete("/api/boards/{id}", boardId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
