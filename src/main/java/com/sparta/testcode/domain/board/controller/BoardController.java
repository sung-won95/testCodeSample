package com.sparta.testcode.domain.board.controller;

import com.sparta.testcode.domain.board.dto.BoardRequestDto;
import com.sparta.testcode.domain.board.dto.BoardResponseDto;
import com.sparta.testcode.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto requestDto) {
        return ResponseEntity.ok(boardService.createBoard(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getBoard(id));
    }

    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getAllBoards() {
        return ResponseEntity.ok(boardService.getAllBoards());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto requestDto) {
        return ResponseEntity.ok(boardService.updateBoard(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
